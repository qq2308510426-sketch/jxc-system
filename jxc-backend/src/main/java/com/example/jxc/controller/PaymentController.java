package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Payment;
import com.example.jxc.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    public Result<List<Payment>> getByOrder(@PathVariable Long orderId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId);
        return Result.success(paymentService.list(wrapper));
    }

    @GetMapping("/list")
    public Result<PageResult<Payment>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Payment::getCreateTime);
        Page<Payment> result = paymentService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @OperationLog(action = "\u521b\u5efa\u4ed8\u6b3e")
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        
        Payment payment = new Payment();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        payment.setPaymentNo("PAY" + dateStr + uuid);
        payment.setOrderId(Long.valueOf(dto.get("orderId").toString()));
        payment.setAmount(new java.math.BigDecimal(dto.get("amount").toString()));
        payment.setPaymentMethod((String) dto.getOrDefault("paymentMethod", "cash"));
        payment.setRemark((String) dto.get("remark"));
        payment.setOperatorId(userId);
        paymentService.save(payment);
        
        return Result.success();
    }
}