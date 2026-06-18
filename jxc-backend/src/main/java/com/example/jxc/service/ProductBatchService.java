package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.ProductBatch;

import java.util.List;

public interface ProductBatchService extends IService<ProductBatch> {
    Page<ProductBatch> listPage(int pageNum, int pageSize, Long productId, String keyword);
    List<ProductBatch> listByProduct(Long productId);
}