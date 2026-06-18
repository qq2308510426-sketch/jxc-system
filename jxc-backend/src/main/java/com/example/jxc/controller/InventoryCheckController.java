package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.InventoryCheck;
import com.example.jxc.entity.InventoryCheckItem;
import com.example.jxc.service.InventoryCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-check")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService checkService;

    @OperationLog(action = "\u521b\u5efa\u76d8\u70b9\u5355")
    @PostMapping
    public Result<Void> create(@RequestBody List<InventoryCheckItem> items, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        checkService.createCheck(items, userId);
        return Result.success();
    }

    @OperationLog(action = "\u5b8c\u6210\u76d8\u70b9")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        checkService.completeCheck(id, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<InventoryCheck>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<InventoryCheck> page = checkService.listPage(pageNum, pageSize);
        PageResult<InventoryCheck> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}/items")
    public Result<List<InventoryCheckItem>> getItems(@PathVariable Long id) {
        return Result.success(checkService.getCheckItems(id));
    }
}