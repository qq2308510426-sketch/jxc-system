package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.dto.AccountPaymentDTO;
import com.example.jxc.entity.*;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.*;
import com.example.jxc.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired private AccountReceivableMapper receivableMapper;
    @Autowired private AccountPayableMapper payableMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private OrderMapper orderMapper;

    @Override
    public List<Map<String, Object>> getReceivableSummary() {
        List<Customer> customers = customerMapper.selectList(null);
        if (customers.isEmpty()) return Collections.emptyList();

        Set<Long> customerIds = customers.stream().map(Customer::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<AccountReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AccountReceivable::getCustomerId, customerIds);
        List<AccountReceivable> allReceivables = receivableMapper.selectList(wrapper);

        Map<Long, List<AccountReceivable>> byCustomer = allReceivables.stream()
            .collect(Collectors.groupingBy(AccountReceivable::getCustomerId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Customer c : customers) {
            List<AccountReceivable> list = byCustomer.getOrDefault(c.getId(), Collections.emptyList());
            if (list.isEmpty()) continue;

            BigDecimal totalAmount = list.stream().map(AccountReceivable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPaid = list.stream().map(AccountReceivable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = totalAmount.subtract(totalPaid);
            long overdueCount = list.stream().filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(java.time.LocalDate.now()) && r.getStatus() != 2).count();

            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getId());
            item.put("customerName", c.getName());
            item.put("totalAmount", totalAmount);
            item.put("paidAmount", totalPaid);
            item.put("outstanding", outstanding);
            item.put("overdueCount", overdueCount);
            item.put("totalCount", list.size());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getPayableSummary() {
        List<Supplier> suppliers = supplierMapper.selectList(null);
        if (suppliers.isEmpty()) return Collections.emptyList();

        Set<Long> supplierIds = suppliers.stream().map(Supplier::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<AccountPayable> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AccountPayable::getSupplierId, supplierIds);
        List<AccountPayable> allPayables = payableMapper.selectList(wrapper);

        Map<Long, List<AccountPayable>> bySupplier = allPayables.stream()
            .collect(Collectors.groupingBy(AccountPayable::getSupplierId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Supplier s : suppliers) {
            List<AccountPayable> list = bySupplier.getOrDefault(s.getId(), Collections.emptyList());
            if (list.isEmpty()) continue;

            BigDecimal totalAmount = list.stream().map(AccountPayable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPaid = list.stream().map(AccountPayable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = totalAmount.subtract(totalPaid);
            long overdueCount = list.stream().filter(p -> p.getDueDate() != null && p.getDueDate().isBefore(java.time.LocalDate.now()) && p.getStatus() != 2).count();

            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", s.getId());
            item.put("supplierName", s.getName());
            item.put("totalAmount", totalAmount);
            item.put("paidAmount", totalPaid);
            item.put("outstanding", outstanding);
            item.put("overdueCount", overdueCount);
            item.put("totalCount", list.size());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getReceivableDetail(Long customerId) {
        LambdaQueryWrapper<AccountReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountReceivable::getCustomerId, customerId)
               .orderByDesc(AccountReceivable::getCreateTime);
        return receivableMapper.selectList(wrapper).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("orderId", r.getOrderId());
            m.put("amount", r.getAmount());
            m.put("paidAmount", r.getPaidAmount());
            m.put("outstanding", r.getAmount().subtract(r.getPaidAmount()));
            m.put("status", r.getStatus());
            m.put("dueDate", r.getDueDate());
            m.put("createTime", r.getCreateTime());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPayableDetail(Long supplierId) {
        LambdaQueryWrapper<AccountPayable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccountPayable::getSupplierId, supplierId)
               .orderByDesc(AccountPayable::getCreateTime);
        return payableMapper.selectList(wrapper).stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("purchaseOrderId", p.getPurchaseOrderId());
            m.put("amount", p.getAmount());
            m.put("paidAmount", p.getPaidAmount());
            m.put("outstanding", p.getAmount().subtract(p.getPaidAmount()));
            m.put("status", p.getStatus());
            m.put("dueDate", p.getDueDate());
            m.put("createTime", p.getCreateTime());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void receivePayment(AccountPaymentDTO dto) {
        AccountReceivable receivable = null;
        if (dto.getAccountId() != null) {
            receivable = receivableMapper.selectById(dto.getAccountId());
        } else if (dto.getCustomerId() != null) {
            LambdaQueryWrapper<AccountReceivable> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccountReceivable::getCustomerId, dto.getCustomerId())
                   .ne(AccountReceivable::getStatus, 2)
                   .orderByAsc(AccountReceivable::getCreateTime)
                   .last("LIMIT 1");
            receivable = receivableMapper.selectOne(wrapper);
        }
        if (receivable == null) {
            throw new BusinessException("\u5e94\u6536\u8d26\u6b3e\u4e0d\u5b58\u5728");
        }
        if (receivable.getStatus() == 2) {
            throw new BusinessException("\u8be5\u8d26\u6b3e\u5df2\u7ed3\u6e05");
        }
        BigDecimal outstanding = receivable.getAmount().subtract(receivable.getPaidAmount());
        if (dto.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessException("\u6536\u6b3e\u91d1\u989d\u8d85\u8fc7\u672a\u6536\u91d1\u989d: " + outstanding);
        }

        receivable.setPaidAmount(receivable.getPaidAmount().add(dto.getAmount()));
        receivable.setStatus(receivable.getPaidAmount().compareTo(receivable.getAmount()) >= 0 ? 2 : 1);
        receivableMapper.updateById(receivable);
    }

    @Override
    @Transactional
    public void makePayment(AccountPaymentDTO dto) {
        AccountPayable payable = null;
        if (dto.getAccountId() != null) {
            payable = payableMapper.selectById(dto.getAccountId());
        } else if (dto.getSupplierId() != null) {
            LambdaQueryWrapper<AccountPayable> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccountPayable::getSupplierId, dto.getSupplierId())
                   .ne(AccountPayable::getStatus, 2)
                   .orderByAsc(AccountPayable::getCreateTime)
                   .last("LIMIT 1");
            payable = payableMapper.selectOne(wrapper);
        }
        if (payable == null) {
            throw new BusinessException("\u5e94\u4ed8\u8d26\u6b3e\u4e0d\u5b58\u5728");
        }
        if (payable.getStatus() == 2) {
            throw new BusinessException("\u8be5\u8d26\u6b3e\u5df2\u7ed3\u6e05");
        }
        BigDecimal outstanding = payable.getAmount().subtract(payable.getPaidAmount());
        if (dto.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessException("\u4ed8\u6b3e\u91d1\u989d\u8d85\u8fc7\u672a\u4ed8\u91d1\u989d: " + outstanding);
        }

        payable.setPaidAmount(payable.getPaidAmount().add(dto.getAmount()));
        payable.setStatus(payable.getPaidAmount().compareTo(payable.getAmount()) >= 0 ? 2 : 1);
        payableMapper.updateById(payable);
    }

    @Override
    public Map<String, Object> getAccountStats() {
        Map<String, Object> stats = new HashMap<>();

        // Receivable stats
        List<AccountReceivable> allReceivables = receivableMapper.selectList(null);
        BigDecimal totalReceivable = allReceivables.stream().map(AccountReceivable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReceived = allReceivables.stream().map(AccountReceivable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long overdueReceivable = allReceivables.stream()
            .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(java.time.LocalDate.now()) && r.getStatus() != 2).count();

        stats.put("totalReceivable", totalReceivable);
        stats.put("totalReceived", totalReceived);
        stats.put("outstandingReceivable", totalReceivable.subtract(totalReceived));
        stats.put("overdueReceivableCount", overdueReceivable);

        // Payable stats
        List<AccountPayable> allPayables = payableMapper.selectList(null);
        BigDecimal totalPayable = allPayables.stream().map(AccountPayable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = allPayables.stream().map(AccountPayable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long overduePayable = allPayables.stream()
            .filter(p -> p.getDueDate() != null && p.getDueDate().isBefore(java.time.LocalDate.now()) && p.getStatus() != 2).count();

        stats.put("totalPayable", totalPayable);
        stats.put("totalPaid", totalPaid);
        stats.put("outstandingPayable", totalPayable.subtract(totalPaid));
        stats.put("overduePayableCount", overduePayable);

        return stats;
    }
}