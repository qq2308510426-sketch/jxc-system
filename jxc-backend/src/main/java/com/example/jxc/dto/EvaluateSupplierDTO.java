package com.example.jxc.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EvaluateSupplierDTO {

    @DecimalMin(value = "0.0", message = "\u8d28\u91cf\u8bc4\u5206\u4e0d\u80fd\u4e3a\u8d1f\u6570")
    private BigDecimal qualityScore;

    @Min(value = 0, message = "\u4ea4\u8d27\u5929\u6570\u4e0d\u80fd\u4e3a\u8d1f\u6570")
    private Integer deliveryDays;
}