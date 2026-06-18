package com.example.jxc.aspect;

import com.example.jxc.entity.Log;
import com.example.jxc.mapper.LogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Build detail with request parameters
        StringBuilder detailBuilder = new StringBuilder();
        if (operationLog.detail() != null && !operationLog.detail().isEmpty()) {
            detailBuilder.append(operationLog.detail());
        }

        // Add request parameters
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String[] paramNames = signature.getParameterNames();
                detailBuilder.append(" | \u53c2\u6570: ");
                for (int i = 0; i < args.length; i++) {
                    if (paramNames != null && i < paramNames.length) {
                        // Skip HttpServletRequest and HttpServletResponse
                        if (args[i] instanceof HttpServletRequest) continue;
                        try {
                            detailBuilder.append(paramNames[i]).append("=").append(objectMapper.writeValueAsString(args[i]));
                            if (i < args.length - 1) detailBuilder.append(", ");
                        } catch (Exception e) {
                            detailBuilder.append(paramNames[i]).append("=[\u5e8f\u5217\u5316\u5931\u8d25]");
                        }
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

        // Write to file
        String logDir = "logs";
        File dir = new File(logDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logContent = String.format("[%s] userId=%s, action=%s, detail=%s, ip=%s",
                timestamp,
                userId != null ? userId.toString() : "null",
                operationLog.action(),
                detail,
                ip);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logDir + "/operation.log", true))) {
            writer.write(logContent);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}