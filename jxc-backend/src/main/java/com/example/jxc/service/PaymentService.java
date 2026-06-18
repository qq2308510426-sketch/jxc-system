package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Payment;

import java.util.List;

public interface PaymentService extends IService<Payment> {

    void createPayment(Long orderId, java.math.BigDecimal amount, String paymentMethod, String remark, Long operatorId);

    Page<Payment> listByOrder(Long orderId, int pageNum, int pageSize);

    Page<Payment> listAll(int pageNum, int pageSize);
}