package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Warehouse;
import com.example.jxc.mapper.WarehouseMapper;
import com.example.jxc.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public Page<Warehouse> listPage(int pageNum, int pageSize, String keyword) {
        Page<Warehouse> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Warehouse::getName, keyword);
        }
        wrapper.orderByDesc(Warehouse::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Warehouse> listAll() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getStatus, 1);
        wrapper.orderByAsc(Warehouse::getName);
        return this.list(wrapper);
    }
}