package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long supplierId;

    private BigDecimal totalAmount;

    private Integer status;

    private BigDecimal paidAmount;

    private Integer paymentStatus;

    private String remark;

    private Long operatorId;

    private Long approverId;

    private LocalDateTime approveTime;

    private LocalDateTime receiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}