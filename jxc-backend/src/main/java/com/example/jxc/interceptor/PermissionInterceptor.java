package com.example.jxc.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.entity.RolePermission;
import com.example.jxc.mapper.RolePermissionMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
        "/api/auth/login",
        "/api/notification/unread-count"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        String role = (String) request.getAttribute("role");
        if (role == null) {
            sendForbidden(response, "\u672a\u77e5\u89d2\u8272");
            return false;
        }

        // Resolve required permission from path + method
        String permission = resolvePermission(path, request.getMethod());
        if (permission == null) {
            sendForbidden(response, "\u672a\u77e5\u8d44\u6e90\u62d2\u7edd\u8bbf\u95ee");
            return false;
        }

        // Query database for role permission (no hardcoded bypass)
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRole, role)
               .eq(RolePermission::getPermission, permission);
        long count = rolePermissionMapper.selectCount(wrapper);

        if (count == 0) {
            sendForbidden(response, "\u65e0\u6743\u8bbf\u95ee: " + permission);
            return false;
        }

        return true;
    }

    private void sendForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\"}");
    }

    private String resolvePermission(String path, String method) {
        // --- Product ---
        if (path.startsWith("/api/product")) {
            if ("POST".equals(method)) return "product:create";
            if ("PUT".equals(method)) return "product:edit";
            if ("DELETE".equals(method)) return "product:delete";
            return "product:view";
        }

        // --- Category ---
        if (path.startsWith("/api/category")) {
            if ("POST".equals(method)) return "category:create";
            if ("PUT".equals(method)) return "category:edit";
            if ("DELETE".equals(method)) return "category:delete";
            return "category:view";
        }

        // --- Supplier ---
        if (path.startsWith("/api/supplier")) {
            if ("POST".equals(method)) return "supplier:create";
            if ("PUT".equals(method)) return "supplier:edit";
            if ("DELETE".equals(method)) return "supplier:delete";
            return "supplier:view";
        }

        // --- Customer ---
        if (path.startsWith("/api/customer")) {
            if ("POST".equals(method)) return "customer:create";
            if ("PUT".equals(method)) return "customer:edit";
            if ("DELETE".equals(method)) return "customer:delete";
            return "customer:view";
        }

        // --- Order ---
        if (path.startsWith("/api/order")) {
            if ("POST".equals(method)) return "order:create";
            if ("PUT".equals(method)) return "order:edit";
            if ("DELETE".equals(method)) return "order:delete";
            if (path.contains("/ship")) return "order:ship";
            return "order:view";
        }

        // --- Return ---
        if (path.startsWith("/api/return")) {
            if ("POST".equals(method) || "PUT".equals(method)) return "order:edit";
            return "order:view";
        }

        // --- Payment ---
        if (path.startsWith("/api/payment")) {
            if ("POST".equals(method)) return "account:receivable";
            return "account:view";
        }

        // --- Stock ---
        if (path.startsWith("/api/stock/in")) {
            if ("POST".equals(method) || "DELETE".equals(method)) return "stock:in";
            return "stock:view";
        }
        if (path.startsWith("/api/stock/out")) {
            if ("POST".equals(method)) return "stock:out";
            return "stock:view";
        }
        if (path.startsWith("/api/stock")) return "stock:view";

        // --- Warehouse ---
        if (path.startsWith("/api/warehouse")) {
            if ("POST".equals(method)) return "warehouse:create";
            if ("PUT".equals(method)) return "warehouse:edit";
            if ("DELETE".equals(method)) return "warehouse:delete";
            return "warehouse:view";
        }

        // --- Product Batch ---
        if (path.startsWith("/api/product-batch")) {
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) return "stock:manage";
            return "stock:view";
        }

        // --- Inventory Check ---
        if (path.startsWith("/api/inventory-check")) {
            if ("POST".equals(method) || "PUT".equals(method)) return "stock:manage";
            return "stock:view";
        }

        // --- Purchase Order ---
        if (path.startsWith("/api/purchase-order")) {
            if ("POST".equals(method)) return "purchase:create";
            if (path.contains("/approve")) return "purchase:approve";
            if (path.contains("/receive")) return "purchase:receive";
            if (path.contains("/cancel")) return "purchase:create";
            return "purchase:view";
        }

        // --- Account ---
        if (path.startsWith("/api/account")) {
            if (path.contains("/receivable/pay")) return "account:receivable";
            if (path.contains("/payable/pay")) return "account:payable";
            return "account:view";
        }

        // --- Report ---
        if (path.startsWith("/api/report")) {
            return "report:view";
        }

        // --- Data Import/Export ---
        if (path.startsWith("/api/data")) {
            if ("POST".equals(method)) return "data:import";
            return "data:export";
        }

        // --- User ---
        if (path.startsWith("/api/user")) {
            if ("POST".equals(method)) return "user:create";
            if ("PUT".equals(method)) return "user:edit";
            if ("DELETE".equals(method)) return "user:delete";
            return "user:view";
        }

        // --- Log ---
        if (path.startsWith("/api/log")) return "log:view";

        // --- Notification ---
        if (path.startsWith("/api/notification")) return "notification:view";

        // Default deny
        return null;
    }
}