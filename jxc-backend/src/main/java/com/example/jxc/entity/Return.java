package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_return")
public class Return {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String returnNo;

    private Long orderId;

    private Long customerId;

    private Integer type;

    private Integer status;

    private String reason;

    private BigDecimal totalAmount;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}