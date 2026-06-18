package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;

public interface StockService extends IService<StockIn> {

    void stockIn(StockOperateDTO dto, Long operatorId);

    void stockOut(StockOperateDTO dto, Long operatorId);

    Page<StockIn> listStockIn(int pageNum, int pageSize);

    Page<StockOut> listStockOut(int pageNum, int pageSize);

    void cancelStockIn(Long stockInId, Long operatorId);
}
