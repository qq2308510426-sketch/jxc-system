package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Supplier;
import com.example.jxc.mapper.SupplierMapper;
import com.example.jxc.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public Page<Supplier> listPage(int pageNum, int pageSize, String keyword) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Supplier::getName, keyword)
                    .or().like(Supplier::getContact, keyword)
                    .or().like(Supplier::getPhone, keyword));
        }

        wrapper.orderByDesc(Supplier::getCreateTime);
        return this.page(page, wrapper);
    }
}