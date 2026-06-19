package com.example.jxc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderCreateDTO {

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String remark;

    @NotEmpty(message = "采购明细不能为空")
    @Valid
    private List<PurchaseItemDTO> items;

    @Data
    public static class PurchaseItemDTO {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer quantity;

        @NotNull(message = "单价不能为空")
        @Min(value = 0, message = "单价不能为负")
        private BigDecimal unitPrice;
    }
}