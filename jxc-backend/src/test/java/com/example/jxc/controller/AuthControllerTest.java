package com.example.jxc.controller;

import com.example.jxc.entity.User;
import com.example.jxc.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        userMapper.delete(null);
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRealName("Admin");
        admin.setRole("admin");
        admin.setStatus(1);
        userMapper.insert(admin);
    }

    @Test
    void loginSuccess() throws Exception {
        Map<String, String> loginReq = Map.of("username", "admin", "password", "admin123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("LOGIN RESPONSE: " + response);
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"token\""), "Should contain token");
    }

    @Test
    void loginWithWrongPassword() throws Exception {
        Map<String, String> loginReq = Map.of("username", "admin", "password", "wrong");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("WRONG PW: " + response);
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("500"), "Should return error");
    }

    @Test
    void accessProtectedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized());
    }
}