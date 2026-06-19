package com.example.jxc.vo;

import com.example.jxc.entity.PurchaseOrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private BigDecimal paidAmount;
    private Integer paymentStatus;
    private String remark;
    private Long operatorId;
    private String operatorName;
    private Long approverId;
    private LocalDateTime approveTime;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;
    private List<PurchaseOrderItem> items;
}