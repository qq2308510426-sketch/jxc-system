package com.example.jxc.controller;

import com.example.jxc.common.Result;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/error")
public class ErrorReportController {

    private static final Logger log = LoggerFactory.getLogger(ErrorReportController.class);

    @PostMapping("/report")
    public Result<Void> report(@RequestBody Map<String, Object> errorInfo) {
        String message = (String) errorInfo.getOrDefault("message", "Unknown error");
        String stack = (String) errorInfo.getOrDefault("stack", "");
        String url = (String) errorInfo.getOrDefault("url", "");
        String userAgent = (String) errorInfo.getOrDefault("userAgent", "");

        log.error("[Frontend Error] url={}, message={}, ua={}, stack={}",
                url, message, userAgent, stack);

        return Result.success();
    }
}
