package com.example.jxc.controller;

import com.example.jxc.entity.*;
import com.example.jxc.mapper.*;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private ProductMapper productMapper;

    private String token;
    private Long categoryId;
    private Long supplierId;

    @BeforeEach
    void setup() {
        // Clean tables
        productMapper.delete(null);
        categoryMapper.delete(null);
        supplierMapper.delete(null);
        userMapper.delete(null);

        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        admin.setRealName("Admin");
        admin.setRole("super_admin");
        admin.setStatus(1);
        userMapper.insert(admin);

        // Get token
        try {
            String resp = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"))))
                    .andReturn().getResponse().getContentAsString();
            token = objectMapper.readTree(resp).get("data").get("token").asText();
        } catch (Exception e) { throw new RuntimeException(e); }

        // Create category
        Category cat = new Category();
        cat.setName("电子产品");
        cat.setParentId(0L);
        cat.setSort(1);
        categoryMapper.insert(cat);
        categoryId = cat.getId();

        // Create supplier
        Supplier sup = new Supplier();
        sup.setName("测试供应商");
        sup.setContact("张三");
        sup.setPhone("13800138000");
        sup.setStatus(1);
        supplierMapper.insert(sup);
        supplierId = sup.getId();
    }

    @Test
    void createProduct() throws Exception {
        Map<String, Object> product = Map.of(
            "name", "iPhone 15",
            "categoryId", categoryId,
            "spec", "256GB",
            "price", 6999,
            "stock", 0,
            "safetyStock", 10,
            "supplierId", supplierId,
            "status", 1
        );

        mockMvc.perform(post("/api/product")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listProducts() throws Exception {
        // Create a product first
        Product p = new Product();
        p.setName("测试商品");
        p.setCategoryId(categoryId);
        p.setPrice(BigDecimal.valueOf(100));
        p.setStock(50);
        p.setSafetyStock(10);
        p.setStatus(1);
        productMapper.insert(p);

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", "Bearer " + token)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rows").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void deleteProduct() throws Exception {
        Product p = new Product();
        p.setName("待删除商品");
        p.setCategoryId(categoryId);
        p.setPrice(BigDecimal.valueOf(50));
        p.setStock(0);
        p.setStatus(1);
        productMapper.insert(p);

        mockMvc.perform(delete("/api/product/" + p.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deleted
        org.junit.jupiter.api.Assertions.assertNull(productMapper.selectById(p.getId()));
    }
}