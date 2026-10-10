package com.example.jxc.config;

import com.example.jxc.util.ClientIpUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 全局限流：每个客户端 IP 每分钟 120 次接口调用，只作用于 /api，放行预检请求。 */
@Component
public class RateLimitConfig implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    private static class RequestCounter {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();

        boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        // 跨域预检不计配额，否则浏览器会拿到 429 并表现为整站请求失败
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())
                || !path.startsWith("/api")
                || "/api/auth/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse res = (HttpServletResponse) response;
        // 经过隧道/反向代理时用 X-Forwarded-For 区分真实访客，避免所有人共用同一个计数桶
        String clientIp = ClientIpUtil.getClientIp(req);
        RequestCounter counter = counters.computeIfAbsent(clientIp, k -> new RequestCounter());

        if (!counter.tryAcquire()) {
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":429,\"message\":\"\u8bf7\u6c42\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\",\"data\":null}");
            return;
        }

        if (counters.size() > 10_000) {
            long now = System.currentTimeMillis();
            counters.entrySet().removeIf(e -> now - e.getValue().windowStart > 120_000);
        }

        chain.doFilter(request, response);
    }
}
