package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.PurchaseOrderCreateDTO;
import com.example.jxc.entity.PurchaseOrder;
import com.example.jxc.service.PurchaseOrderService;
import com.example.jxc.vo.PurchaseOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-order")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @OperationLog(action = "\u521b\u5efa\u91c7\u8d2d\u5355")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PurchaseOrderCreateDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        purchaseOrderService.createPurchaseOrder(dto, userId);
        return Result.success();
    }

    @OperationLog(action = "\u5ba1\u6279\u91c7\u8d2d\u5355")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        purchaseOrderService.approvePurchaseOrder(id, userId);
        return Result.success();
    }

    @OperationLog(action = "\u91c7\u8d2d\u6536\u8d27")
    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        purchaseOrderService.receivePurchaseOrder(id, userId);
        return Result.success();
    }

    @OperationLog(action = "\u53d6\u6d88\u91c7\u8d2d\u5355")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        purchaseOrderService.cancelPurchaseOrder(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> detail(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getOrderDetail(id));
    }

    @GetMapping("/list")
    public Result<PageResult<PurchaseOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long supplierId) {
        Page<PurchaseOrder> page = purchaseOrderService.listPage(pageNum, pageSize, status, supplierId);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords()));
    }
}