package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;
import com.example.jxc.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @OperationLog(action = "\u5165\u5e93\u64cd\u4f5c")
    @PostMapping("/in")
    public Result<Void> stockIn(@Valid @RequestBody StockOperateDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        stockService.stockIn(dto, userId);
        return Result.success();
    }

    @OperationLog(action = "\u51fa\u5e93\u64cd\u4f5c")
    @PostMapping("/out")
    public Result<Void> stockOut(@Valid @RequestBody StockOperateDTO dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        stockService.stockOut(dto, userId);
        return Result.success();
    }

    @GetMapping("/in/list")
    public Result<PageResult<StockIn>> listStockIn(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<StockIn> page = stockService.listStockIn(pageNum, pageSize);
        PageResult<StockIn> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/out/list")
    public Result<PageResult<StockOut>> listStockOut(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<StockOut> page = stockService.listStockOut(pageNum, pageSize);
        PageResult<StockOut> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @OperationLog(action = "\u64a4\u9500\u5165\u5e93")
    @DeleteMapping("/in/{id}")
    public Result<Void> cancelStockIn(@PathVariable Long id, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        stockService.cancelStockIn(id, userId);
        return Result.success();
    }
}