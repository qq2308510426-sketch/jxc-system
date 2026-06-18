package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.ProductBatch;
import com.example.jxc.mapper.ProductBatchMapper;
import com.example.jxc.service.ProductBatchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductBatchServiceImpl extends ServiceImpl<ProductBatchMapper, ProductBatch> implements ProductBatchService {

    @Override
    public Page<ProductBatch> listPage(int pageNum, int pageSize, Long productId, String keyword) {
        Page<ProductBatch> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductBatch> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(ProductBatch::getProductId, productId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProductBatch::getBatchNo, keyword)
                    .or().like(ProductBatch::getSerialNo, keyword));
        }
        wrapper.orderByDesc(ProductBatch::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<ProductBatch> listByProduct(Long productId) {
        LambdaQueryWrapper<ProductBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductBatch::getProductId, productId);
        wrapper.eq(ProductBatch::getStatus, 1);
        wrapper.orderByAsc(ProductBatch::getExpiryDate);
        return this.list(wrapper);
    }
}