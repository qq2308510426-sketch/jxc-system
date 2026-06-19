package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.InventoryCheck;
import com.example.jxc.entity.InventoryCheckItem;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.InventoryCheckItemMapper;
import com.example.jxc.service.InventoryCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-check")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @Autowired
    private InventoryCheckItemMapper inventoryCheckItemMapper;

    @GetMapping("/list")
    public Result<PageResult<InventoryCheck>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<InventoryCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InventoryCheck::getCreateTime);
        Page<InventoryCheck> result = inventoryCheckService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @OperationLog(action = "\u521b\u5efa\u76d8\u70b9")
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        
        InventoryCheck check = new InventoryCheck();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        check.setCheckNo("CHK" + dateStr + uuid);
        check.setRemark((String) dto.get("remark"));
        check.setOperatorId(userId);
        check.setStatus(0);
        inventoryCheckService.save(check);
        
        return Result.success();
    }

    @OperationLog(action = "\u5b8c\u6210\u76d8\u70b9")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        InventoryCheck check = inventoryCheckService.getById(id);
        if (check == null) {
            throw new BusinessException("\u76d8\u70b9\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        check.setStatus(1);
        inventoryCheckService.updateById(check);
        return Result.success();
    }

    @GetMapping("/{id}/items")
    public Result<List<InventoryCheckItem>> items(@PathVariable Long id) {
        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, id);
        return Result.success(inventoryCheckItemMapper.selectList(wrapper));
    }
}