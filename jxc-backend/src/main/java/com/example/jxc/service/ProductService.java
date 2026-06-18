package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Product;

public interface ProductService extends IService<Product> {

    Page<Product> listPage(int pageNum, int pageSize, String keyword, Long categoryId);
}