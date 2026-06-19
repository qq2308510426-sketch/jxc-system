package com.example.jxc.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShipOrderDTO {

    @Size(max = 100, message = "\u7269\u6d41\u5355\u53f7\u4e0d\u80fd\u8d85\u8fc7100\u5b57\u7b26")
    private String shippingNo;

    @Size(max = 100, message = "\u7269\u6d41\u516c\u53f8\u4e0d\u80fd\u8d85\u8fc7100\u5b57\u7b26")
    private String shippingCompany;
}