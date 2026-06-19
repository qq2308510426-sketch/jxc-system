package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.ChangePasswordDTO;
import com.example.jxc.dto.CreateUserDTO;
import com.example.jxc.dto.UpdateUserDTO;
import com.example.jxc.entity.RolePermission;
import com.example.jxc.entity.User;
import com.example.jxc.mapper.RolePermissionMapper;
import com.example.jxc.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

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

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(page, wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (User u : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("role", u.getRole());
            map.put("roleLabel", ROLE_LABELS.getOrDefault(u.getRole(), u.getRole()));
            map.put("status", u.getStatus());
            map.put("warehouseId", u.getWarehouseId());
            map.put("createTime", u.getCreateTime());
            records.add(map);
        }
        return Result.success(new PageResult<>(result.getTotal(), records));
    }

    @GetMapping("/roles")
    public Result<Map<String, String>> getRoles() {
        return Result.success(ROLE_LABELS);
    }

    @OperationLog(action = "\u65b0\u589e\u7528\u6237")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CreateUserDTO dto) {
        // Check username uniqueness
        LambdaQueryWrapper<User> check = new LambdaQueryWrapper<>();
        check.eq(User::getUsername, dto.getUsername());
        if (userService.count(check) > 0) {
            return Result.error(400, "\u7528\u6237\u540d\u5df2\u5b58\u5728");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setWarehouseId(dto.getWarehouseId());
        user.setStatus(1);
        userService.save(user);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u7528\u6237")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UpdateUserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        user.setWarehouseId(dto.getWarehouseId());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userService.updateById(user);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u7528\u6237")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();
        if (id.equals(currentUserId)) {
            return Result.error(400, "\u4e0d\u80fd\u5220\u9664\u81ea\u5df1\u7684\u8d26\u53f7");
        }
        userService.removeById(id);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        User user = userService.getById(userId);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("roleLabel", ROLE_LABELS.getOrDefault(user.getRole(), user.getRole()));
        userInfo.put("warehouseId", user.getWarehouseId());

        // Get permissions
        List<String> permissions;
        if ("super_admin".equals(user.getRole())) {
            permissions = Arrays.asList("all");
        } else {
            LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RolePermission::getRole, user.getRole());
            List<RolePermission> perms = rolePermissionMapper.selectList(wrapper);
            permissions = perms.stream().map(RolePermission::getPermission).collect(Collectors.toList());
        }
        userInfo.put("permissions", permissions);

        return Result.success(userInfo);
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @GetMapping("/permissions")
    public Result<List<String>> getPermissions(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if ("super_admin".equals(role)) {
            return Result.success(Arrays.asList("all"));
        }
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRole, role);
        List<RolePermission> perms = rolePermissionMapper.selectList(wrapper);
        return Result.success(perms.stream().map(RolePermission::getPermission).collect(Collectors.toList()));
    }
}