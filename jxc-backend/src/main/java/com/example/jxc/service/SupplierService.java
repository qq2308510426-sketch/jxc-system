package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Supplier;

public interface SupplierService extends IService<Supplier> {

    Page<Supplier> listPage(int pageNum, int pageSize, String keyword);
}