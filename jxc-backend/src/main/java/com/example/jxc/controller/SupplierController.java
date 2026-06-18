package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Supplier;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.StockIn;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @GetMapping("/list")
    public Result<PageResult<Supplier>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Supplier> page = supplierService.listPage(pageNum, pageSize, keyword);
        PageResult<Supplier> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LambdaQueryWrapper<Product> prodWrapper = new LambdaQueryWrapper<>();
        prodWrapper.eq(Product::getSupplierId, id);
        if (productMapper.selectCount(prodWrapper) > 0) {
            throw new BusinessException("该供应商有关联商品，无法删除");
        }
        LambdaQueryWrapper<StockIn> siWrapper = new LambdaQueryWrapper<>();
        siWrapper.eq(StockIn::getSupplierId, id);
        if (stockInMapper.selectCount(siWrapper) > 0) {
            throw new BusinessException("该供应商有关联入库记录，无法删除");
        }
        supplierService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/evaluate")
    public Result<Void> evaluate(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            return Result.error(404, "供应商不存在");
        }
        if (params.containsKey("qualityScore") && params.get("qualityScore") != null) {
            supplier.setQualityScore(new java.math.BigDecimal(params.get("qualityScore").toString()));
        }
        if (params.containsKey("deliveryDays")) {
            supplier.setDeliveryDays((Integer) params.get("deliveryDays"));
        }
        supplierService.updateById(supplier);
        return Result.success();
    }
}