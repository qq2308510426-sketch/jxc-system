package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long customerId;

    private BigDecimal totalAmount;

    private Integer status;

    private String remark;

    private Long operatorId;

    private String shippingNo;

    private String shippingCompany;

    private Integer shippingStatus;

    private LocalDateTime shippingTime;

    private Integer paymentStatus;

    private BigDecimal paidAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}