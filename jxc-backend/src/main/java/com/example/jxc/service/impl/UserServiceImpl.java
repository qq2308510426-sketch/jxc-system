package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.RolePermission;
import com.example.jxc.entity.User;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.RolePermissionMapper;
import com.example.jxc.mapper.UserMapper;
import com.example.jxc.service.UserService;
import com.example.jxc.util.JwtUtil;
import com.example.jxc.config.LoginRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired private UserMapper userMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private LoginRateLimiter loginRateLimiter;
    @Autowired private RolePermissionMapper rolePermissionMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Map<String, String> ROLE_LABELS = new LinkedHashMap<>();
    static {
        ROLE_LABELS.put("super_admin", "\u8d85\u7ea7\u7ba1\u7406\u5458");
        ROLE_LABELS.put("warehouse_admin", "\u4ed3\u5e93\u7ba1\u7406\u5458");
        ROLE_LABELS.put("purchaser", "\u91c7\u8d2d\u5458");
        ROLE_LABELS.put("salesperson", "\u9500\u552e\u5458");
        ROLE_LABELS.put("finance", "\u8d22\u52a1");
        ROLE_LABELS.put("viewer", "\u67e5\u770b\u8005");
    }

    @Override
    public Map<String, Object> login(String username, String password, String ip) {
        if (loginRateLimiter.isBlocked(username, ip)) {
            throw new BusinessException("\u767b\u5f55\u5c1d\u8bd5\u6b21\u6570\u8fc7\u591a\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);

        if (user == null) {
            loginRateLimiter.recordFailure(username, ip);
            throw new BusinessException("\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("\u7528\u6237\u5df2\u7981\u7528");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginRateLimiter.recordFailure(username, ip);
            throw new BusinessException("\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef");
        }

        loginRateLimiter.resetAttempts(username, ip);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // Get permissions
        List<String> permissions;
        if ("super_admin".equals(user.getRole())) {
            permissions = Arrays.asList("all");
        } else {
            LambdaQueryWrapper<RolePermission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.eq(RolePermission::getRole, user.getRole());
            List<RolePermission> perms = rolePermissionMapper.selectList(permWrapper);
            permissions = perms.stream().map(RolePermission::getPermission).collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("roleLabel", ROLE_LABELS.getOrDefault(user.getRole(), user.getRole()));
        userInfo.put("warehouseId", user.getWarehouseId());
        userInfo.put("permissions", permissions);
        result.put("token", token);
        result.put("userInfo", userInfo);
        return result;
    }

    @Override
    public void updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        this.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("\u539f\u5bc6\u7801\u4e0d\u6b63\u786e");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("\u65b0\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e6\u4f4d");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}