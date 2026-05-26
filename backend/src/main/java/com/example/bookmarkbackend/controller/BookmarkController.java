package com.example.bookmarkbackend.controller;

import com.example.bookmarkbackend.entity.Bookmark;
import com.example.bookmarkbackend.service.BookmarkService;
import com.example.bookmarkbackend.util.Result;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bookmark")
public class BookmarkController {

    @Resource private BookmarkService bookmarkService;

    @GetMapping("/list")
    public Result<List<Bookmark>> list(Long userId) {
        List<Bookmark> list = bookmarkService.lambdaQuery().eq(Bookmark::getUserId, userId).list();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Bookmark> add(@RequestBody Bookmark bookmark) {
        bookmarkService.saveOrUpdate(bookmark);
        return Result.success(bookmark);
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody Bookmark bookmark) {
        bookmarkService.removeById(bookmark.getId());
        return Result.success();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam Long userId, @RequestParam(defaultValue = "json") String format) {
        String content;
        String filename;
        String contentType;

        if ("html".equalsIgnoreCase(format)) {
            content = bookmarkService.exportAsHtml(userId);
            filename = "bookmarks.html";
            contentType = "text/html";
        } else {
            content = bookmarkService.exportAsJson(userId);
            filename = "bookmarks.json";
            contentType = "application/json";
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType + "; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @PostMapping("/import")
    public Result<Integer> importBookmarks(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            int count;

            if ("html".equalsIgnoreCase(format)) {
                count = bookmarkService.importFromHtml(content, userId);
            } else {
                count = bookmarkService.importFromJson(content, userId);
            }

            return Result.success(count);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
