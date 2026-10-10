package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;

public interface StockService extends IService<StockIn> {

    void stockIn(StockOperateDTO dto, Long operatorId);

    void stockOut(StockOperateDTO dto, Long operatorId);

    /**
     * 退货回补：加总库存、写入库流水与分仓台账，但不重算加权平均成本
     * （回补按现行成本结转，只有采购入库才改变成本口径）。
     *
     * @return 本次回补归属的仓库ID，定位不到时为 null
     */
    Long returnToStock(StockOperateDTO dto, Long operatorId);

    /** 操作人所属仓库；超级管理员或未绑仓返回 null。 */
    Long resolveWarehouseId(Long operatorId);

    Page<StockIn> listStockIn(int pageNum, int pageSize, Long operatorId);

    Page<StockOut> listStockOut(int pageNum, int pageSize, Long operatorId);

    void cancelStockIn(Long stockInId, Long operatorId);

    /**
     * 按仓库维度增减库存台账；warehouseId 为空时不处理。
     */
    void adjustWarehouseStock(Long productId, Long warehouseId, int delta);
}
