package com.example.bookmarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookmarkbackend.entity.Bookmark;
import com.example.bookmarkbackend.entity.Category;
import com.example.bookmarkbackend.mapper.BookmarkMapper;
import com.example.bookmarkbackend.service.BookmarkService;
import com.example.bookmarkbackend.service.CategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BookmarkServiceImpl extends ServiceImpl<BookmarkMapper, Bookmark>
        implements BookmarkService {

    @Resource private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String exportAsJson(Long userId) {
        List<Category> categories = categoryService.listByUserId(userId);
        List<Bookmark> bookmarks = lambdaQuery().eq(Bookmark::getUserId, userId).list();

        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("bookmarks", bookmarks);

        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("导出 JSON 失败", e);
        }
    }

    @Override
    public String exportAsHtml(Long userId) {
        List<Category> categories = categoryService.listByUserId(userId);
        List<Bookmark> bookmarks = lambdaQuery().eq(Bookmark::getUserId, userId).list();

        Map<Long, List<Category>> childrenMap =
                categories.stream()
                        .filter(c -> c.getParentId() != null)
                        .collect(Collectors.groupingBy(Category::getParentId));

        Map<Long, List<Bookmark>> bookmarkMap =
                bookmarks.stream()
                        .filter(b -> b.getCategoryId() != null)
                        .collect(Collectors.groupingBy(Bookmark::getCategoryId));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n");
        sb.append(
                "<!-- This is an automatically generated file.\n"
                        + "     It will be read and overwritten.\n"
                        + "     DO NOT EDIT! -->\n");
        sb.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n");
        sb.append("<TITLE>Bookmarks</TITLE>\n");
        sb.append("<H1>书签菜单</H1>\n");
        sb.append("<DL><p>\n");

        for (Category category : categories) {
            if (category.getParentId() == null) {
                buildCategoryHtml(sb, category, childrenMap, bookmarkMap, 1);
            }
        }

        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getCategoryId() == null) {
                appendIndent(sb, 1);
                sb.append("<DT><A HREF=\"")
                        .append(escapeHtml(bookmark.getUrl()))
                        .append("\">")
                        .append(escapeHtml(bookmark.getTitle()))
                        .append("</A>\n");
            }
        }

        sb.append("</DL><p>\n");
        return sb.toString();
    }

    private void buildCategoryHtml(
            StringBuilder sb,
            Category category,
            Map<Long, List<Category>> childrenMap,
            Map<Long, List<Bookmark>> bookmarkMap,
            int depth) {
        appendIndent(sb, depth);
        sb.append("<DT><H3>").append(escapeHtml(category.getName())).append("</H3>\n");
        appendIndent(sb, depth);
        sb.append("<DL><p>\n");

        List<Bookmark> categoryBookmarks =
                bookmarkMap.getOrDefault(category.getId(), new ArrayList<>());
        for (Bookmark bookmark : categoryBookmarks) {
            appendIndent(sb, depth + 1);
            sb.append("<DT><A HREF=\"")
                    .append(escapeHtml(bookmark.getUrl()))
                    .append("\">")
                    .append(escapeHtml(bookmark.getTitle()))
                    .append("</A>\n");
        }

        List<Category> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
        for (Category child : children) {
            buildCategoryHtml(sb, child, childrenMap, bookmarkMap, depth + 1);
        }

        appendIndent(sb, depth);
        sb.append("</DL><p>\n");
    }

    private void appendIndent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    @Transactional
    public int importFromJson(String json, Long userId) {
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

            List<Map<String, Object>> categoryMaps =
                    (List<Map<String, Object>>) data.get("categories");
            List<Map<String, Object>> bookmarkMaps =
                    (List<Map<String, Object>>) data.get("bookmarks");

            Map<Long, Long> categoryIdMap = new HashMap<>();

            if (categoryMaps != null) {
                for (Map<String, Object> cm : categoryMaps) {
                    String name = (String) cm.get("name");
                    Long oldId = Long.valueOf(cm.get("id").toString());
                    Long parentId =
                            cm.get("parentId") != null
                                    ? Long.valueOf(cm.get("parentId").toString())
                                    : null;

                    Category existing = categoryService.getByNameAndUserId(name, userId);
                    Category category;
                    if (existing != null) {
                        category = existing;
                    } else {
                        category = new Category();
                        category.setName(name);
                        category.setUserId(userId);
                        category.setParentId(parentId != null ? categoryIdMap.get(parentId) : null);
                        category.setSortOrder(
                                cm.get("sortOrder") != null
                                        ? Integer.valueOf(cm.get("sortOrder").toString())
                                        : 0);
                        categoryService.save(category);
                    }
                    categoryIdMap.put(oldId, category.getId());
                }
            }

            int count = 0;
            if (bookmarkMaps != null) {
                for (Map<String, Object> bm : bookmarkMaps) {
                    String url = (String) bm.get("url");
                    Long categoryId =
                            bm.get("categoryId") != null
                                    ? Long.valueOf(bm.get("categoryId").toString())
                                    : null;

                    boolean exists =
                            lambdaQuery()
                                    .eq(Bookmark::getUrl, url)
                                    .eq(Bookmark::getUserId, userId)
                                    .exists();
                    if (!exists) {
                        Bookmark bookmark = new Bookmark();
                        bookmark.setTitle((String) bm.get("title"));
                        bookmark.setUrl(url);
                        bookmark.setUserId(userId);
                        bookmark.setCategoryId(
                                categoryId != null ? categoryIdMap.get(categoryId) : null);
                        save(bookmark);
                        count++;
                    }
                }
            }

            return count;
        } catch (Exception e) {
            throw new RuntimeException("导入 JSON 失败", e);
        }
    }

    @Override
    @Transactional
    public int importFromHtml(String html, Long userId) {
        try {
            log.info("开始导入 HTML，用户ID: {}, HTML长度: {}", userId, html.length());
            Document doc = Jsoup.parse(html);
            Element rootDl = doc.select("dl").first();
            if (rootDl == null) {
                log.error("未找到 <dl> 根元素");
                throw new RuntimeException("HTML 格式错误：未找到 <dl> 标签");
            }
            log.info("找到根 <dl> 元素，开始解析");

            List<Bookmark> imported = new ArrayList<>();
            parseDlElement(rootDl, null, userId, imported);
            log.info("解析完成，共找到 {} 个书签", imported.size());

            int count = 0;
            for (Bookmark bookmark : imported) {
                boolean exists =
                        lambdaQuery()
                                .eq(Bookmark::getUrl, bookmark.getUrl())
                                .eq(Bookmark::getUserId, userId)
                                .exists();
                if (!exists) {
                    save(bookmark);
                    count++;
                }
            }

            log.info("导入完成，新增 {} 个书签", count);
            return count;
        } catch (Exception e) {
            log.error("导入 HTML 失败", e);
            throw new RuntimeException("导入 HTML 失败: " + e.getMessage(), e);
        }
    }

    private void parseDlElement(
            Element dl, Long parentCategoryId, Long userId, List<Bookmark> result) {
        if (dl == null) {
            log.warn("parseDlElement: dl 为 null，跳过");
            return;
        }

        List<Element> dtElements = dl.select("dt");
        log.debug("在 <dl> 中找到 {} 个 <dt> 元素", dtElements.size());

        for (Element dt : dtElements) {
            Element h3 = dt.selectFirst("h3");
            Element a = dt.selectFirst("> a");

            if (h3 != null) {
                String folderName = h3.text().trim();
                log.debug("找到文件夹: {}", folderName);
                Category existing = categoryService.getByNameAndUserId(folderName, userId);
                Category category;
                if (existing != null) {
                    category = existing;
                    log.debug("分类已存在: {}", folderName);
                } else {
                    category = new Category();
                    category.setName(folderName);
                    category.setUserId(userId);
                    category.setParentId(parentCategoryId);
                    category.setSortOrder(0);
                    categoryService.save(category);
                    log.debug("创建新分类: {}", folderName);
                }

                Element subDl = dt.selectFirst("dl");
                parseDlElement(subDl, category.getId(), userId, result);
            } else if (a != null) {
                String url = a.attr("href");
                String title = a.text().trim();

                if (url != null && !url.isEmpty()) {
                    Bookmark bookmark = new Bookmark();
                    bookmark.setTitle(title);
                    bookmark.setUrl(url);
                    bookmark.setUserId(userId);
                    bookmark.setCategoryId(parentCategoryId);
                    result.add(bookmark);
                    log.debug("找到书签: {} -> {}", title, url);
                }
            }
        }
    }
}
