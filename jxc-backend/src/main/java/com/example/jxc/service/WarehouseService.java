package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Warehouse;

import java.util.List;

public interface WarehouseService extends IService<Warehouse> {
    Page<Warehouse> listPage(int pageNum, int pageSize, String keyword);
    List<Warehouse> listAll();
}