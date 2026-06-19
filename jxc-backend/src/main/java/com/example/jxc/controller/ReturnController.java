package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ReturnItemMapper;
import com.example.jxc.service.ReturnService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/return")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private ReturnItemMapper returnItemMapper;

    @GetMapping("/list")
    public Result<PageResult<Return>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Page<Return> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Return> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Return::getStatus, status);
        }
        wrapper.orderByDesc(Return::getCreateTime);
        Page<Return> result = returnService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @OperationLog(action = "\u521b\u5efa\u9000\u8d27\u5355")
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> dto, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        
        Return returnObj = new Return();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        returnObj.setReturnNo("RET" + dateStr + uuid);
        returnObj.setType((Integer) dto.get("type"));
        returnObj.setCustomerId(dto.get("customerId") != null ? Long.valueOf(dto.get("customerId").toString()) : null);
        returnObj.setReason((String) dto.get("reason"));
        returnObj.setStatus(0);
        returnObj.setOperatorId(userId);
        
        // Calculate total from items
        List<Map<String, Object>> items = (List<Map<String, Object>>) dto.get("items");
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (Map<String, Object> item : items) {
                BigDecimal price = new BigDecimal(item.get("unitPrice").toString());
                int qty = (Integer) item.get("quantity");
                total = total.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }
        returnObj.setTotalAmount(total);
        returnService.save(returnObj);
        
        // Save items
        if (items != null) {
            for (Map<String, Object> item : items) {
                ReturnItem ri = new ReturnItem();
                ri.setReturnId(returnObj.getId());
                ri.setProductId(Long.valueOf(item.get("productId").toString()));
                ri.setQuantity((Integer) item.get("quantity"));
                ri.setUnitPrice(new BigDecimal(item.get("unitPrice").toString()));
                ri.setSubtotal(ri.getUnitPrice().multiply(BigDecimal.valueOf(ri.getQuantity())));
                returnItemMapper.insert(ri);
            }
        }
        
        return Result.success();
    }

    @OperationLog(action = "\u5904\u7406\u9000\u8d27\u5355")
    @PutMapping("/{id}/process")
    public Result<Void> process(@PathVariable Long id, @RequestParam Integer status) {
        Return returnObj = returnService.getById(id);
        if (returnObj == null) {
            throw new BusinessException("\u9000\u8d27\u5355\u4e0d\u5b58\u5728");
        }
        returnObj.setStatus(status);
        returnService.updateById(returnObj);
        return Result.success();
    }

    @GetMapping("/{id}/items")
    public Result<List<ReturnItem>> items(@PathVariable Long id) {
        LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnItem::getReturnId, id);
        return Result.success(returnItemMapper.selectList(wrapper));
    }
}