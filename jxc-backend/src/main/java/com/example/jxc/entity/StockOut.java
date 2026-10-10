package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_stock_out")
public class StockOut {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long customerId;

    private Long orderId;

    private Long warehouseId;

    private Integer quantity;

    private BigDecimal unitPrice;

    /** 出库时点结转的加权平均成本，用于事后毛利核算 */
    private BigDecimal costPrice;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
