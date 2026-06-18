package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;
import com.example.jxc.service.ReturnService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/return")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @OperationLog(action = "\u521b\u5efa\u9000\u8d27\u5355")
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();

        Return returnObj = new Return();
        returnObj.setOrderId(params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null);
        returnObj.setCustomerId(params.get("customerId") != null ? Long.valueOf(params.get("customerId").toString()) : null);
        returnObj.setType(params.get("type") != null ? Integer.valueOf(params.get("type").toString()) : 1);
        returnObj.setReason((String) params.get("reason"));
        returnObj.setOperatorId(userId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) params.get("items");
        List<ReturnItem> items = new java.util.ArrayList<>();
        if (itemMaps != null) {
            for (Map<String, Object> itemMap : itemMaps) {
                ReturnItem item = new ReturnItem();
                if (itemMap.get("productId") == null || itemMap.get("quantity") == null) {
                    continue;
                }
                item.setProductId(Long.valueOf(itemMap.get("productId").toString()));
                item.setQuantity(Integer.valueOf(itemMap.get("quantity").toString()));
                java.math.BigDecimal unitPrice = itemMap.get("unitPrice") != null
                    ? new java.math.BigDecimal(itemMap.get("unitPrice").toString())
                    : java.math.BigDecimal.ZERO;
                item.setUnitPrice(unitPrice);
                item.setSubtotal(unitPrice.multiply(new java.math.BigDecimal(item.getQuantity())));
                items.add(item);
            }
        }

        returnService.createReturn(returnObj, items);
        return Result.success();
    }

    @OperationLog(action = "\u5904\u7406\u9000\u8d27\u5355")
    @PutMapping("/{id}/process")
    public Result<Void> process(@PathVariable Long id, @RequestParam int status, HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        returnService.processReturn(id, status, userId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Return>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Page<Return> page = returnService.listPage(pageNum, pageSize, status);
        PageResult<Return> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}/items")
    public Result<List<ReturnItem>> getItems(@PathVariable Long id) {
        return Result.success(returnService.getReturnItems(id));
    }
}