package com.example.jxc.aspect;

import com.example.jxc.entity.Log;
import com.example.jxc.mapper.LogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(com.example.jxc.aspect.OperationLog)", returning = "result")
    public void recordLog(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        Object userIdObj = request.getAttribute("userId");
        Long userId = userIdObj != null ? ((Number) userIdObj).longValue() : null;
        String ip = request.getRemoteAddr();

        StringBuilder detailBuilder = new StringBuilder();
        if (operationLog.detail() != null && !operationLog.detail().isEmpty()) {
            detailBuilder.append(operationLog.detail());
        }

        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String[] paramNames = signature.getParameterNames();
                detailBuilder.append(" | \u53c2\u6570: ");
                for (int i = 0; i < args.length; i++) {
                    if (paramNames != null && i < paramNames.length) {
                        if (args[i] instanceof HttpServletRequest) continue;
                        String paramName = paramNames[i];
                        if (isSensitiveParam(paramName)) {
                            detailBuilder.append(paramName).append("=***");
                        } else {
                            try {
                                detailBuilder.append(paramName).append("=").append(objectMapper.writeValueAsString(args[i]));
                            } catch (Exception e) {
                                detailBuilder.append(paramName).append("=[\u5e8f\u5217\u5316\u5931\u8d25]");
                            }
                        }
                        if (i < args.length - 1) detailBuilder.append(", ");
                    }
                }
            }
        } catch (Exception e) {
            // Ignore serialization errors
        }

        String detail = detailBuilder.toString();

        Log log = new Log();
        log.setUserId(userId);
        log.setAction(operationLog.action());
        log.setDetail(detail);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    private String maskSensitiveJson(String json) {
        if (json == null) return json;
        String[] fields = {"password", "oldPassword", "newPassword", "confirmPassword"};
        for (String field : fields) {
            String search = "\"" + field + "\"";
            int idx = 0;
            while ((idx = json.toLowerCase().indexOf(search.toLowerCase(), idx)) != -1) {
                int colonIdx = json.indexOf(":", idx + search.length());
                if (colonIdx == -1) break;
                int quoteStart = json.indexOf("\"", colonIdx + 1);
                if (quoteStart == -1) break;
                int quoteEnd = json.indexOf("\"", quoteStart + 1);
                if (quoteEnd == -1) break;
                json = json.substring(0, quoteStart + 1) + "***" + json.substring(quoteEnd);
                idx = quoteStart + 4;
            }
        }
        return json;
    }

    private boolean isSensitiveParam(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("password") || lower.contains("passwd")
            || lower.contains("secret") || lower.contains("token")
            || lower.contains("oldpassword") || lower.contains("newpassword");
    }
}