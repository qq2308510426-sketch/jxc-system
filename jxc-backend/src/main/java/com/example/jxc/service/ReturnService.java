package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;

import java.util.List;

public interface ReturnService extends IService<Return> {

    void createReturn(Return returnObj, List<ReturnItem> items);

    void processReturn(Long returnId, int status, Long operatorId);

    Page<Return> listPage(int pageNum, int pageSize, Integer status);

    List<ReturnItem> getReturnItems(Long returnId);
}