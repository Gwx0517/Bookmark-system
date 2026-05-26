package com.example.bookmarkbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookmarkbackend.entity.Category;
import com.example.bookmarkbackend.mapper.CategoryMapper;
import com.example.bookmarkbackend.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Override
    public List<Category> listByUserId(Long userId) {
        return lambdaQuery().eq(Category::getUserId, userId).list();
    }

    @Override
    public Category getByNameAndUserId(String name, Long userId) {
        return lambdaQuery().eq(Category::getName, name).eq(Category::getUserId, userId).one();
    }
}
