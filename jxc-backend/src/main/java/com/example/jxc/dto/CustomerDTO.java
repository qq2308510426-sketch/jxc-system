package com.example.jxc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "\u5ba2\u6237\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 100, message = "\u5ba2\u6237\u540d\u79f0\u4e0d\u80fd\u8d85\u8fc7100\u5b57\u7b26")
    private String name;

    @Size(max = 50, message = "\u8054\u7cfb\u4eba\u4e0d\u80fd\u8d85\u8fc750\u5b57\u7b26")
    private String contact;

    @Size(max = 20, message = "\u7535\u8bdd\u4e0d\u80fd\u8d85\u8fc720\u5b57\u7b26")
    private String phone;

    @Size(max = 255, message = "\u5730\u5740\u4e0d\u80fd\u8d85\u8fc7255\u5b57\u7b26")
    private String address;

    @Size(max = 20, message = "\u4fe1\u7528\u7b49\u7ea7\u4e0d\u80fd\u8d85\u8fc720\u5b57\u7b26")
    private String creditLevel;

    @DecimalMin(value = "0.00", message = "\u4fe1\u7528\u989d\u5ea6\u4e0d\u80fd\u4e3a\u8d1f\u6570")
    private BigDecimal creditLimit;

    @Size(max = 500, message = "\u5907\u6ce8\u4e0d\u80fd\u8d85\u8fc7500\u5b57\u7b26")
    private String remark;

    private Integer status;
}