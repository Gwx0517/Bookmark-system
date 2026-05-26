package com.example.bookmarkbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookmarkbackend.entity.Bookmark;

public interface BookmarkService extends IService<Bookmark> {

    String exportAsJson(Long userId);

    String exportAsHtml(Long userId);

    int importFromJson(String json, Long userId);

    int importFromHtml(String html, Long userId);
}
