package com.example.jxc.config;

import com.example.jxc.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class FileUploadValidator {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif",
        "application/pdf",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/csv"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("\u6587\u4ef6\u4e3a\u7a7a");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("\u6587\u4ef6\u5927\u5c0f\u8d85\u8fc7\u9650\u5236 (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("\u4e0d\u652f\u6301\u7684\u6587\u4ef6\u7c7b\u578b: " + contentType);
        }
    }
}