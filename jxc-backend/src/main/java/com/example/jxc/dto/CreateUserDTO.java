package com.example.jxc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDTO {

    @NotBlank(message = "\u7528\u6237\u540d\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(min = 3, max = 50, message = "\u7528\u6237\u540d\u957f\u5ea6\u5fc5\u987b\u57283-50\u4f4d\u4e4b\u95f4")
    private String username;

    @NotBlank(message = "\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a")
    @Size(min = 6, max = 100, message = "\u5bc6\u7801\u957f\u5ea6\u5fc5\u987b\u57286-100\u4f4d\u4e4b\u95f4")
    private String password;

    private String realName;

    @NotBlank(message = "\u89d2\u8272\u4e0d\u80fd\u4e3a\u7a7a")
    private String role;

    private Long warehouseId;
}