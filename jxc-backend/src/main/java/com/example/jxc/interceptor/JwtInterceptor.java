package com.example.jxc.interceptor;

import com.example.jxc.config.TokenBlacklist;
import com.example.jxc.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"\u672a\u63d0\u4f9b\u8ba4\u8bc1\u4ee4\u724c\"}");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (tokenBlacklist.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"\u4ee4\u724c\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55\"}");
            return false;
        }

        if (!jwtUtil.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"\u65e0\u6548\u7684\u8ba4\u8bc1\u4ee4\u724c\"}");
            return false;
        }

        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("username", claims.getSubject());
        request.setAttribute("role", claims.get("role"));

        return true;
    }
}