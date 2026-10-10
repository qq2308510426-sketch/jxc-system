package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.AccountReceivable;
import com.example.jxc.entity.Order;
import com.example.jxc.entity.Payment;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.AccountReceivableMapper;
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

    @Autowired
    private AccountReceivableMapper receivableMapper;

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

        String paymentNo = "PAY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

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

        // 同一笔收款同时核销该订单的应收账款，避免"付款记录"和"应收应付"两套账
        LambdaQueryWrapper<AccountReceivable> arWrapper = new LambdaQueryWrapper<>();
        arWrapper.eq(AccountReceivable::getOrderId, orderId)
                 .ne(AccountReceivable::getStatus, 2)
                 .orderByAsc(AccountReceivable::getCreateTime);
        BigDecimal remain = amount;
        for (AccountReceivable ar : receivableMapper.selectList(arWrapper)) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal outstanding = ar.getAmount().subtract(ar.getPaidAmount());
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal applied = outstanding.min(remain);
            ar.setPaidAmount(ar.getPaidAmount().add(applied));
            remain = remain.subtract(applied);
            ar.setStatus(ar.getPaidAmount().compareTo(ar.getAmount()) >= 0 ? 2 : 1);
            receivableMapper.updateById(ar);
        }
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