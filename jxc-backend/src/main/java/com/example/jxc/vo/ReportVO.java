package com.example.jxc.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportVO {

    // Sales report fields
    private String period;
    private Integer orderCount;
    private BigDecimal totalSales;
    private BigDecimal totalCost;
    private BigDecimal profit;

    // Stock report fields
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer safetyStock;
    private Boolean warning;
}
