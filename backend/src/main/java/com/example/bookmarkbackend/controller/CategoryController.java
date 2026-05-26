package com.example.bookmarkbackend.controller;

import com.example.bookmarkbackend.entity.Bookmark;
import com.example.bookmarkbackend.entity.Category;
import com.example.bookmarkbackend.service.BookmarkService;
import com.example.bookmarkbackend.service.CategoryService;
import com.example.bookmarkbackend.util.Result;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource private CategoryService categoryService;
    @Resource private BookmarkService bookmarkService;

    @GetMapping("/list")
    public Result<List<Category>> list(Long userId) {
        List<Category> list =
                categoryService
                        .lambdaQuery()
                        .eq(Category::getUserId, userId)
                        .orderByAsc(Category::getSortOrder)
                        .list();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Category> add(@RequestBody Category category) {
        categoryService.saveOrUpdate(category);
        return Result.success(category);
    }

    @PostMapping("/batchUpdate")
    public Result batchUpdate(@RequestBody List<Category> categories) {
        if (categories != null && !categories.isEmpty()) {
            categoryService.updateBatchById(categories);
        }
        return Result.success();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody Category category) {
        List<Long> idsToDelete = new ArrayList<>();
        collectCategoryIdsRecursive(category.getId(), idsToDelete);

        if (!idsToDelete.isEmpty()) {
            bookmarkService.lambdaUpdate().in(Bookmark::getCategoryId, idsToDelete).remove();

            categoryService.removeByIds(idsToDelete);
        }

        return Result.success();
    }

    private void collectCategoryIdsRecursive(Long parentId, List<Long> ids) {
        ids.add(parentId);
        List<Category> children =
                categoryService.lambdaQuery().eq(Category::getParentId, parentId).list();
        for (Category child : children) {
            collectCategoryIdsRecursive(child.getId(), ids);
        }
    }
}
