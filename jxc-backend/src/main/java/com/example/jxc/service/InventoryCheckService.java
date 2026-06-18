package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.InventoryCheck;
import com.example.jxc.entity.InventoryCheckItem;

import java.util.List;

public interface InventoryCheckService extends IService<InventoryCheck> {

    void createCheck(List<InventoryCheckItem> items, Long operatorId);

    void completeCheck(Long checkId, Long operatorId);

    Page<InventoryCheck> listPage(int pageNum, int pageSize);

    List<InventoryCheckItem> getCheckItems(Long checkId);
}