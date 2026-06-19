package com.example.jxc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
@Configuration
public class PaginationConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(int.class, "pageSize", new java.beans.PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                int value = Integer.parseInt(text);
                if (value > 100) {
                    value = 100;
                }
                if (value < 1) {
                    value = 10;
                }
                setValue(value);
            }
        });
    }
}