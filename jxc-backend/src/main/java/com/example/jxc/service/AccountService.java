package com.example.jxc.service;

import com.example.jxc.dto.AccountPaymentDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AccountService {

    List<Map<String, Object>> getReceivableSummary();

    List<Map<String, Object>> getPayableSummary();

    List<Map<String, Object>> getReceivableDetail(Long customerId);

    List<Map<String, Object>> getPayableDetail(Long supplierId);

    void receivePayment(AccountPaymentDTO dto);

    void makePayment(AccountPaymentDTO dto);

    Map<String, Object> getAccountStats();
}