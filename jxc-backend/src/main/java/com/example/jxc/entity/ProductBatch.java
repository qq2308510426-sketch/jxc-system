package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_product_batch")
public class ProductBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String batchNo;

    private String serialNo;

    private Integer quantity;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private Long supplierId;

    private BigDecimal purchasePrice;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}