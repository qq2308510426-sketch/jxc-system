package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_account_receivable")
public class AccountReceivable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long orderId;

    private BigDecimal amount;

    private BigDecimal paidAmount;

    private Integer status;

    private String remark;

    private LocalDate dueDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}