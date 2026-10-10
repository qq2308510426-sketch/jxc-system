package com.example.jxc.util;

import jakarta.servlet.http.HttpServletRequest;

/** 反向代理或内网穿透场景下获取真实客户端 IP。 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (isUsable(forwarded)) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (isUsable(real)) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }

    private static boolean isUsable(String value) {
        return value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value);
    }
}
