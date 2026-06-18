package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.ProductBatch;
import com.example.jxc.service.ProductBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-batch")
public class ProductBatchController {

    @Autowired
    private ProductBatchService batchService;

    @GetMapping("/list")
    public Result<PageResult<ProductBatch>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String keyword) {
        Page<ProductBatch> page = batchService.listPage(pageNum, pageSize, productId, keyword);
        return Result.success(new PageResult<>(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProductBatch>> listByProduct(@PathVariable Long productId) {
        return Result.success(batchService.listByProduct(productId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody ProductBatch batch) {
        batchService.save(batch);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ProductBatch batch) {
        batchService.updateById(batch);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        batchService.removeById(id);
        return Result.success();
    }
}