package com.example.jxc.controller;

import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.Result;
import com.example.jxc.dto.AccountPaymentDTO;
import com.example.jxc.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(accountService.getAccountStats());
    }

    @GetMapping("/receivable/summary")
    public Result<List<Map<String, Object>>> receivableSummary() {
        return Result.success(accountService.getReceivableSummary());
    }

    @GetMapping("/payable/summary")
    public Result<List<Map<String, Object>>> payableSummary() {
        return Result.success(accountService.getPayableSummary());
    }

    @GetMapping("/receivable/detail/{customerId}")
    public Result<List<Map<String, Object>>> receivableDetail(@PathVariable Long customerId) {
        return Result.success(accountService.getReceivableDetail(customerId));
    }

    @GetMapping("/payable/detail/{supplierId}")
    public Result<List<Map<String, Object>>> payableDetail(@PathVariable Long supplierId) {
        return Result.success(accountService.getPayableDetail(supplierId));
    }

    @OperationLog(action = "\u6536\u6b3e\u786e\u8ba4")
    @PostMapping("/receivable/pay")
    public Result<Void> receivePayment(@Valid @RequestBody AccountPaymentDTO dto) {
        accountService.receivePayment(dto);
        return Result.success();
    }

    @OperationLog(action = "\u4ed8\u6b3e\u786e\u8ba4")
    @PostMapping("/payable/pay")
    public Result<Void> makePayment(@Valid @RequestBody AccountPaymentDTO dto) {
        accountService.makePayment(dto);
        return Result.success();
    }
}