package com.example.jxc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "\u5546\u54c1\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(max = 200, message = "\u5546\u54c1\u540d\u79f0\u4e0d\u80fd\u8d85\u8fc7200\u5b57\u7b26")
    private String name;

    private Long categoryId;

    @Size(max = 200, message = "\u89c4\u683c\u4e0d\u80fd\u8d85\u8fc7200\u5b57\u7b26")
    private String spec;

    @NotNull(message = "\u4ef7\u683c\u4e0d\u80fd\u4e3a\u7a7a")
    @DecimalMin(value = "0.01", message = "\u4ef7\u683c\u5fc5\u987b\u5927\u4e8e0")
    private BigDecimal price;

    @Min(value = 0, message = "\u5b89\u5168\u5e93\u5b58\u4e0d\u80fd\u4e3a\u8d1f\u6570")
    private Integer safetyStock;

    private Long supplierId;

    @Size(max = 500, message = "\u56fe\u7247\u5730\u5740\u4e0d\u80fd\u8d85\u8fc7500\u5b57\u7b26")
    private String imageUrl;

    private Integer status;
}