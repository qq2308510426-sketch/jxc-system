package com.example.jxc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {

    @NotNull(message = "\u7528\u6237ID\u4e0d\u80fd\u4e3a\u7a7a")
    private Long id;

    private String realName;

    private String role;

    private Integer status;

    private String password;

    private Long warehouseId;
}