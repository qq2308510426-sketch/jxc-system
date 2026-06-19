package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.OrderCreateDTO;
import com.example.jxc.dto.ShipOrderDTO;
import com.example.jxc.entity.Order;
import com.example.jxc.service.OrderService;
import com.example.jxc.util.PrintUtil;
import com.example.jxc.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @OperationLog(action = "\u521b\u5efa\u8ba2\u5355")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody OrderCreateDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        orderService.createOrder(dto, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long customerId) {
        Page<Order> page = orderService.listPage(pageNum, pageSize, status, customerId);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @OperationLog(action = "\u8ba2\u5355\u72b6\u6001\u53d8\u66f4")
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable int status) {
        orderService.updateStatus(id, status);
        return Result.success();
    }

    @OperationLog(action = "\u8ba2\u5355\u53d1\u8d27")
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody ShipOrderDTO dto) {
        orderService.shipOrder(id, dto.getShippingNo(), dto.getShippingCompany());
        return Result.success();
    }

    @GetMapping("/pending/{customerId}")
    public Result<List<Order>> getPendingOrders(@PathVariable Long customerId) {
        return Result.success(orderService.getPendingOrders(customerId));
    }

    @GetMapping("/{id}/print")
    public Result<String> getPrintContent(@PathVariable Long id) {
        OrderVO order = orderService.getOrderDetail(id);
        String html = PrintUtil.generateOrderPrintHtml(order, order.getItems() != null ? order.getItems() : new ArrayList<>());
        return Result.success(html);
    }
}