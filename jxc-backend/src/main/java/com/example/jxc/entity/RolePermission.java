package com.example.jxc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_role_permission")
public class RolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String role;

    private String permission;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}