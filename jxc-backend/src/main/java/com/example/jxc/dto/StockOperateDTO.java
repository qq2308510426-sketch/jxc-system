package com.example.jxc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOperateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Long supplierId;

    private Long customerId;

    private Long orderId;

    private Long warehouseId;

    private String batchNo;

    private String serialNo;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;
}