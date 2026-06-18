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
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;

    private String token;
    private Long productId;
    private Long customerId;

    @BeforeEach
    void setup() {
        orderItemMapper.delete(null);
        orderMapper.delete(null);
        productMapper.delete(null);
        customerMapper.delete(null);
        supplierMapper.delete(null);
        categoryMapper.delete(null);
        userMapper.delete(null);

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

        Category cat = new Category(); cat.setName("分类"); cat.setParentId(0L);
        categoryMapper.insert(cat);

        Supplier sup = new Supplier(); sup.setName("供应商"); sup.setStatus(1);
        supplierMapper.insert(sup);

        Customer cust = new Customer(); cust.setName("客户A"); cust.setStatus(1);
        customerMapper.insert(cust);
        customerId = cust.getId();

        Product p = new Product();
        p.setName("商品A");
        p.setCategoryId(cat.getId());
        p.setPrice(BigDecimal.valueOf(100));
        p.setStock(200);
        p.setSafetyStock(10);
        p.setSupplierId(sup.getId());
        p.setStatus(1);
        productMapper.insert(p);
        productId = p.getId();
    }

    @Test
    void createOrder() throws Exception {
        Map<String, Object> req = Map.of(
            "customerId", customerId,
            "remark", "测试订单",
            "items", List.of(
                Map.of("productId", productId, "quantity", 5, "unitPrice", 100)
            )
        );

        mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void orderTotalCalculatedCorrectly() throws Exception {
        Map<String, Object> req = Map.of(
            "customerId", customerId,
            "remark", "金额测试",
            "items", List.of(
                Map.of("productId", productId, "quantity", 3, "unitPrice", 200)
            )
        );

        String resp = mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Verify total = 3 * 200 = 600
        var orders = orderMapper.selectList(null);
        org.junit.jupiter.api.Assertions.assertEquals(1, orders.size());
        org.junit.jupiter.api.Assertions.assertEquals(0, orders.get(0).getTotalAmount().compareTo(BigDecimal.valueOf(600)));
    }

    @Test
    void updateOrderStatus() throws Exception {
        // Create order
        Map<String, Object> req = Map.of(
            "customerId", customerId,
            "remark", "状态测试",
            "items", List.of(
                Map.of("productId", productId, "quantity", 1, "unitPrice", 100)
            )
        );

        String resp = mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();

        var orders = orderMapper.selectList(null);
        Long orderId = orders.get(0).getId();

        // Update status to 1 (已审核)
        mockMvc.perform(put("/api/order/" + orderId + "/status/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Order updated = orderMapper.selectById(orderId);
        org.junit.jupiter.api.Assertions.assertEquals(1, updated.getStatus());
    }
}