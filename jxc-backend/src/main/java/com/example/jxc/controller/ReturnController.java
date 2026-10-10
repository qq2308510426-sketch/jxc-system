package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;
import com.example.jxc.mapper.ReturnItemMapper;
import com.example.jxc.service.ReturnService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        returnObj.setType(toInt(dto.get("type")));
        returnObj.setCustomerId(toLong(dto.get("customerId")));
        returnObj.setSupplierId(toLong(dto.get("supplierId")));
        returnObj.setOrderId(toLong(dto.get("orderId")));
        returnObj.setReason((String) dto.get("reason"));
        returnObj.setOperatorId(userId);

        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) dto.get("items");
        List<ReturnItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Map<String, Object> item : rawItems) {
                ReturnItem ri = new ReturnItem();
                ri.setProductId(toLong(item.get("productId")));
                ri.setQuantity(toInt(item.get("quantity")));
                Object price = item.get("unitPrice");
                ri.setUnitPrice(price == null ? null : new BigDecimal(price.toString()));
                items.add(ri);
            }
        }

        // 单号、状态、金额与业务校验统一由服务层完成，避免两处生成规则不一致
        returnService.createReturn(returnObj, items);
        return Result.success();
    }

    private static Integer toInt(Object v) {
        return v == null ? null : Integer.valueOf(v.toString());
    }

    private static Long toLong(Object v) {
        return v == null ? null : Long.valueOf(v.toString());
    }

    @OperationLog(action = "\u5904\u7406\u9000\u8d27\u5355")
    @PutMapping("/{id}/process")
    public Result<Void> process(@PathVariable Long id, @RequestParam Integer status,
                                HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        returnService.processReturn(id, status, userId);
        return Result.success();
    }

    @GetMapping("/{id}/items")
    public Result<List<ReturnItem>> items(@PathVariable Long id) {
        LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnItem::getReturnId, id);
        return Result.success(returnItemMapper.selectList(wrapper));
    }
}