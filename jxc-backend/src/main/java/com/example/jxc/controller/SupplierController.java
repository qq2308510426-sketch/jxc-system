package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.SupplierDTO;
import com.example.jxc.entity.Supplier;
import com.example.jxc.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/list")
    public Result<PageResult<Supplier>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Supplier::getName, keyword);
        }
        wrapper.orderByDesc(Supplier::getCreateTime);
        Page<Supplier> result = supplierService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @OperationLog(action = "\u65b0\u589e\u4f9b\u5e94\u5546")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContact(dto.getContact());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setRemark(dto.getRemark());
        supplier.setStatus(1);
        supplierService.save(supplier);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u4f9b\u5e94\u5546")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setId(dto.getId());
        supplier.setName(dto.getName());
        supplier.setContact(dto.getContact());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setRemark(dto.getRemark());
        if (dto.getStatus() != null) supplier.setStatus(dto.getStatus());
        supplierService.updateById(supplier);
        return Result.success();
    }

    @OperationLog(action = "\u4f9b\u5e94\u5546\u8bc4\u4f30")
    @PutMapping("/{id}/evaluate")
    public Result<Void> evaluate(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        Supplier supplier = supplierService.getById(id);
        if (supplier != null) {
            if (params.containsKey("qualityScore")) {
                supplier.setQualityScore(new java.math.BigDecimal(params.get("qualityScore").toString()));
            }
            if (params.containsKey("deliveryDays")) {
                supplier.setDeliveryDays((Integer) params.get("deliveryDays"));
            }
            supplierService.updateById(supplier);
        }
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u4f9b\u5e94\u5546")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.removeById(id);
        return Result.success();
    }
}