package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Log;

public interface LogService extends IService<Log> {

    Page<Log> listPage(int pageNum, int pageSize);
}
