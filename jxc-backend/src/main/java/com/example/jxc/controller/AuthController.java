package com.example.jxc.controller;

import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.Result;
import com.example.jxc.config.TokenBlacklist;
import com.example.jxc.dto.LoginDTO;
import com.example.jxc.service.UserService;
import com.example.jxc.util.ClientIpUtil;
import com.example.jxc.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @OperationLog(action = "\u7528\u6237\u767b\u5f55", detail = "\u7528\u6237\u767b\u5f55\u7cfb\u7edf")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = ClientIpUtil.getClientIp(request);
        Map<String, Object> data = userService.login(dto.getUsername(), dto.getPassword(), ip);
        return Result.success(data);
    }

    @OperationLog(action = "\u7528\u6237\u767b\u51fa")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklist(token, jwtExpiration);
        }
        return Result.success();
    }

}