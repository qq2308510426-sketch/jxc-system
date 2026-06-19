package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long categoryId;

    private String spec;

    private BigDecimal price;

    private Integer stock;

    private Integer reservedStock;

    private Integer safetyStock;

    private Long supplierId;

    private BigDecimal costPrice;

    private String imageUrl;

    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
