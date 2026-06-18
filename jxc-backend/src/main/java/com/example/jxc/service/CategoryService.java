package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<Category> getTree();
}