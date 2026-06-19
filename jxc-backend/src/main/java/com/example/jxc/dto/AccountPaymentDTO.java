package com.example.jxc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountPaymentDTO {

    private Long accountId;

    @NotNull(message = "付款金额不能为空")
    @Min(value = 1, message = "付款金额必须大于0")
    private BigDecimal amount;

    private String remark;

    private Long supplierId;
    private Long customerId;
}