package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.dto.PurchaseOrderCreateDTO;
import com.example.jxc.entity.PurchaseOrder;
import com.example.jxc.vo.PurchaseOrderVO;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    void createPurchaseOrder(PurchaseOrderCreateDTO dto, Long operatorId);

    void approvePurchaseOrder(Long orderId, Long approverId);

    void receivePurchaseOrder(Long orderId, Long operatorId);

    void cancelPurchaseOrder(Long orderId, Long operatorId);

    PurchaseOrderVO getOrderDetail(Long orderId);

    Page<PurchaseOrder> listPage(int pageNum, int pageSize, Integer status, Long supplierId);
}