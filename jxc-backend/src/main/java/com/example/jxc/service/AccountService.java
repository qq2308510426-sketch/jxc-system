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

    /** 收款：核销应收并同步生成收付款流水与订单已收金额。 */
    void receivePayment(AccountPaymentDTO dto, Long operatorId);

    void makePayment(AccountPaymentDTO dto);

    Map<String, Object> getAccountStats();
}