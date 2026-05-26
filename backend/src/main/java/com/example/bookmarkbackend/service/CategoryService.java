package com.example.bookmarkbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookmarkbackend.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> listByUserId(Long userId);

    Category getByNameAndUserId(String name, Long userId);
}
