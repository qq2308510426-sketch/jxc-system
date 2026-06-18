package com.example.jxc.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.entity.RolePermission;
import com.example.jxc.mapper.RolePermissionMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    private static final Set<String> PUBLIC_PATHS = new HashSet<>();

    static {
        PUBLIC_PATHS.add("/api/auth/login");
        PUBLIC_PATHS.add("/api/notification/unread-count");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        String role = (String) request.getAttribute("role");
        if (role == null) {
            return true;
        }

        if ("admin".equals(role)) {
            return true;
        }

        String permission = resolvePermission(path, request.getMethod());
        if (permission == null) {
            return true;
        }

        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRole, role)
               .eq(RolePermission::getPermission, permission);
        long count = rolePermissionMapper.selectCount(wrapper);

        if (count == 0) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"\u65e0\u6743\u8bbf\u95ee\"}");
            return false;
        }

        return true;
    }

    private String resolvePermission(String path, String method) {
        if (path.startsWith("/api/product")) return "product:view";
        if (path.startsWith("/api/order")) return "order:view";
        if (path.startsWith("/api/stock/in")) return "stock:in";
        if (path.startsWith("/api/stock/out")) return "stock:out";
        if (path.startsWith("/api/stock")) return "stock:view";
        if (path.startsWith("/api/user")) return "user:view";
        if (path.startsWith("/api/report")) return "report:view";
        if (path.startsWith("/api/warehouse")) return "stock:view";
        if (path.startsWith("/api/product-batch")) return "stock:view";
        if (path.startsWith("/api/payment")) return "order:view";
        if (path.startsWith("/api/category")) return "product:view";
        if (path.startsWith("/api/supplier")) return "product:view";
        if (path.startsWith("/api/customer")) return "order:view";
        return null;
    }
}