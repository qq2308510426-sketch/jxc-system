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
class StockControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private StockInMapper stockInMapper;
    @Autowired private StockOutMapper stockOutMapper;

    private String token;
    private Long productId;
    private Long supplierId;
    private Long customerId;

    @BeforeEach
    void setup() {
        stockOutMapper.delete(null);
        stockInMapper.delete(null);
        productMapper.delete(null);
        customerMapper.delete(null);
        supplierMapper.delete(null);
        categoryMapper.delete(null);
        userMapper.delete(null);

        // Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        admin.setRealName("Admin");
        admin.setRole("admin");
        admin.setStatus(1);
        userMapper.insert(admin);

        try {
            String resp = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"))))
                    .andReturn().getResponse().getContentAsString();
            token = objectMapper.readTree(resp).get("data").get("token").asText();
        } catch (Exception e) { throw new RuntimeException(e); }

        // Category
        Category cat = new Category();
        cat.setName("测试分类");
        cat.setParentId(0L);
        categoryMapper.insert(cat);

        // Supplier
        Supplier sup = new Supplier();
        sup.setName("测试供应商");
        sup.setStatus(1);
        supplierMapper.insert(sup);
        supplierId = sup.getId();

        // Customer
        Customer cust = new Customer();
        cust.setName("测试客户");
        cust.setCreditLevel("A");
        cust.setStatus(1);
        customerMapper.insert(cust);
        customerId = cust.getId();

        // Product (stock=0)
        Product p = new Product();
        p.setName("测试商品");
        p.setCategoryId(cat.getId());
        p.setPrice(BigDecimal.valueOf(100));
        p.setStock(0);
        p.setSafetyStock(10);
        p.setSupplierId(supplierId);
        p.setStatus(1);
        productMapper.insert(p);
        productId = p.getId();
    }

    @Test
    void stockIn() throws Exception {
        Map<String, Object> req = Map.of(
            "productId", productId,
            "supplierId", supplierId,
            "quantity", 100,
            "unitPrice", 50
        );

        mockMvc.perform(post("/api/stock/in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify stock updated
        Product p = productMapper.selectById(productId);
        org.junit.jupiter.api.Assertions.assertEquals(100, p.getStock());
    }

    @Test
    void stockOut() throws Exception {
        // First stock in
        Product p = productMapper.selectById(productId);
        p.setStock(50);
        productMapper.updateById(p);

        Map<String, Object> req = Map.of(
            "productId", productId,
            "customerId", customerId,
            "quantity", 10,
            "unitPrice", 80
        );

        mockMvc.perform(post("/api/stock/out")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify stock decreased
        Product updated = productMapper.selectById(productId);
        org.junit.jupiter.api.Assertions.assertEquals(40, updated.getStock());
    }

    @Test
    void stockOutExceedsInventory() throws Exception {
        // Stock is 0, try to stock out
        Map<String, Object> req = Map.of(
            "productId", productId,
            "customerId", customerId,
            "quantity", 100,
            "unitPrice", 80
        );

        mockMvc.perform(post("/api/stock/out")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}