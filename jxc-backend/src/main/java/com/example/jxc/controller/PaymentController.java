package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Payment;
import com.example.jxc.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
@Validated
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<Void> create(@RequestBody PaymentCreateDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        paymentService.createPayment(dto.getOrderId(), dto.getAmount(), dto.getPaymentMethod(), dto.getRemark(), userId);
        return Result.success();
    }

    @GetMapping("/order/{orderId}")
    public Result<PageResult<Payment>> listByOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Payment> page = paymentService.listByOrder(orderId, pageNum, pageSize);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/list")
    public Result<PageResult<Payment>> listAll(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Payment> page = paymentService.listAll(pageNum, pageSize);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords()));
    }

    public static class PaymentCreateDTO {
        @NotNull(message = "\u8ba2\u5355ID\u4e0d\u80fd\u4e3a\u7a7a")
        private Long orderId;

        @NotNull(message = "\u4ed8\u6b3e\u91d1\u989d\u4e0d\u80fd\u4e3a\u7a7a")
        @Min(value = 1, message = "\u4ed8\u6b3e\u91d1\u989d\u5fc5\u987b\u5927\u4e8e0")
        private BigDecimal amount;

        private String paymentMethod = "cash";

        private String remark;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}