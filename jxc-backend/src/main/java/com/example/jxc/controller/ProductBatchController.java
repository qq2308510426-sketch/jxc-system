package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.ProductBatch;
import com.example.jxc.service.ProductBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-batch")
public class ProductBatchController {

    @Autowired
    private ProductBatchService productBatchService;

    @GetMapping("/list")
    public Result<PageResult<ProductBatch>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ProductBatch> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProductBatch::getCreateTime);
        Page<ProductBatch> result = productBatchService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/product/{productId}")
    public Result<java.util.List<ProductBatch>> getByProduct(@PathVariable Long productId) {
        LambdaQueryWrapper<ProductBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductBatch::getProductId, productId);
        return Result.success(productBatchService.list(wrapper));
    }

    @OperationLog(action = "\u65b0\u589e\u6279\u6b21")
    @PostMapping
    public Result<Void> create(@RequestBody ProductBatch batch) {
        batch.setStatus(1);
        productBatchService.save(batch);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u6279\u6b21")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productBatchService.removeById(id);
        return Result.success();
    }
}