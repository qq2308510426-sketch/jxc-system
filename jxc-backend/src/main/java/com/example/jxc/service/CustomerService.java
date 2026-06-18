package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Customer;

public interface CustomerService extends IService<Customer> {

    Page<Customer> listPage(int pageNum, int pageSize, String keyword);
}