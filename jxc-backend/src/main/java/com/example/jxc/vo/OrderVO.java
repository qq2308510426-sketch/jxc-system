package com.example.jxc.vo;

import com.example.jxc.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Long customerId;

    private String customerName;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<OrderItem> items;
}