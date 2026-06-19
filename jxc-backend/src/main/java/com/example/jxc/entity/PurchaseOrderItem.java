package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_order_item")
public class PurchaseOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long purchaseOrderId;

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private Integer receivedQuantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}