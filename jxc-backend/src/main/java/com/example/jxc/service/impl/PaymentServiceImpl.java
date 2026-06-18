package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Order;
import com.example.jxc.entity.Payment;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.OrderMapper;
import com.example.jxc.mapper.PaymentMapper;
import com.example.jxc.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public void createPayment(Long orderId, BigDecimal amount, String paymentMethod, String remark, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("\u4ed8\u6b3e\u91d1\u989d\u5fc5\u987b\u5927\u4e8e0");
        }

        BigDecimal remaining = order.getTotalAmount().subtract(order.getPaidAmount());
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException("\u4ed8\u6b3e\u91d1\u989d\u8d85\u8fc7\u5e94\u4ed8\u91d1\u989d: " + remaining);
        }

        String paymentNo = "PAY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", (int)(Math.random() * 10000));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentNo(paymentNo);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setRemark(remark);
        payment.setOperatorId(operatorId);
        paymentMapper.insert(payment);

        BigDecimal newPaidAmount = order.getPaidAmount().add(amount);
        order.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentStatus(2);
        } else {
            order.setPaymentStatus(1);
        }
        orderMapper.updateById(order);
    }

    @Override
    public Page<Payment> listByOrder(Long orderId, int pageNum, int pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Payment::getOrderId, orderId).orderByDesc(Payment::getCreateTime);
        return paymentMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<Payment> listAll(int pageNum, int pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Payment::getCreateTime);
        return paymentMapper.selectPage(page, wrapper);
    }
}