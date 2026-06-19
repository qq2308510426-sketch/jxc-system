package com.example.jxc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "\u4f9b\u5e94\u5546\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 100, message = "\u4f9b\u5e94\u5546\u540d\u79f0\u4e0d\u80fd\u8d85\u8fc7100\u5b57\u7b26")
    private String name;

    @Size(max = 50, message = "\u8054\u7cfb\u4eba\u4e0d\u80fd\u8d85\u8fc750\u5b57\u7b26")
    private String contact;

    @Size(max = 20, message = "\u7535\u8bdd\u4e0d\u80fd\u8d85\u8fc720\u5b57\u7b26")
    private String phone;

    @Size(max = 255, message = "\u5730\u5740\u4e0d\u80fd\u8d85\u8fc7255\u5b57\u7b26")
    private String address;

    @Size(max = 500, message = "\u5907\u6ce8\u4e0d\u80fd\u8d85\u8fc7500\u5b57\u7b26")
    private String remark;

    private Integer status;
}