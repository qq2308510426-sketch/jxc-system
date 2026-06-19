package com.example.jxc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ReturnCreateDTO {

    private Long orderId;

    private Long customerId;

    @NotNull(message = "\u9000\u8d27\u7c7b\u578b\u4e0d\u80fd\u4e3a\u7a7a")
    @Min(value = 1, message = "\u9000\u8d27\u7c7b\u578b\u65e0\u6548")
    private Integer type;

    @Size(max = 500, message = "\u9000\u8d27\u539f\u56e0\u4e0d\u80fd\u8d85\u8fc7500\u5b57\u7b26")
    private String reason;

    @NotEmpty(message = "\u9000\u8d27\u5546\u54c1\u4e0d\u80fd\u4e3a\u7a7a")
    private List<ReturnItemDTO> items;

    @Data
    public static class ReturnItemDTO {
        @NotNull(message = "\u5546\u54c1ID\u4e0d\u80fd\u4e3a\u7a7a")
        private Long productId;

        @NotNull(message = "\u6570\u91cf\u4e0d\u80fd\u4e3a\u7a7a")
        @Min(value = 1, message = "\u6570\u91cf\u5fc5\u987b\u5927\u4e8e0")
        private Integer quantity;

        @DecimalMin(value = "0.00", message = "\u5355\u4ef7\u4e0d\u80fd\u4e3a\u8d1f\u6570")
        private BigDecimal unitPrice;
    }
}