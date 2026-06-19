package com.example.jxc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.ProductDTO;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.*;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockInMapper stockInMapper;
    @Autowired
    private StockOutMapper stockOutMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Autowired
    private ReturnItemMapper returnItemMapper;
    @Autowired
    private ProductBatchMapper productBatchMapper;
    @Autowired
    private InventoryCheckItemMapper inventoryCheckItemMapper;
    @Autowired
    private ProductStockMapper productStockMapper;

    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Page<Product> page = productService.listPage(pageNum, pageSize, keyword, categoryId);
        PageResult<Product> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @OperationLog(action = "\u65b0\u589e\u5546\u54c1")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setCategoryId(dto.getCategoryId());
        product.setSpec(dto.getSpec());
        product.setPrice(dto.getPrice());
        product.setSafetyStock(dto.getSafetyStock());
        product.setSupplierId(dto.getSupplierId());
        product.setImageUrl(dto.getImageUrl());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        product.setStock(0);
        productService.save(product);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u5546\u54c1")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("\u5546\u54c1ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setCategoryId(dto.getCategoryId());
        product.setSpec(dto.getSpec());
        product.setPrice(dto.getPrice());
        product.setSafetyStock(dto.getSafetyStock());
        product.setSupplierId(dto.getSupplierId());
        product.setImageUrl(dto.getImageUrl());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        productService.updateById(product);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u5546\u54c1")
    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Result<Void> delete(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }
        if (product.getStock() != null && product.getStock() > 0) {
            throw new BusinessException("\u8be5\u5546\u54c1\u5f53\u524d\u5e93\u5b58\u4e3a " + product.getStock() + "\uff0c\u8bf7\u5148\u5c06\u5e93\u5b58\u6e05\u96f6\u540e\u518d\u5220\u9664");
        }
        // Cascade delete all related records
        LambdaQueryWrapper<StockIn> siW = new LambdaQueryWrapper<>();
        siW.eq(StockIn::getProductId, id);
        stockInMapper.delete(siW);

        LambdaQueryWrapper<StockOut> soW = new LambdaQueryWrapper<>();
        soW.eq(StockOut::getProductId, id);
        stockOutMapper.delete(soW);

        LambdaQueryWrapper<OrderItem> oiW = new LambdaQueryWrapper<>();
        oiW.eq(OrderItem::getProductId, id);
        orderItemMapper.delete(oiW);

        LambdaQueryWrapper<PurchaseOrderItem> poiW = new LambdaQueryWrapper<>();
        poiW.eq(PurchaseOrderItem::getProductId, id);
        purchaseOrderItemMapper.delete(poiW);

        LambdaQueryWrapper<ReturnItem> riW = new LambdaQueryWrapper<>();
        riW.eq(ReturnItem::getProductId, id);
        returnItemMapper.delete(riW);

        LambdaQueryWrapper<ProductBatch> pbW = new LambdaQueryWrapper<>();
        pbW.eq(ProductBatch::getProductId, id);
        productBatchMapper.delete(pbW);

        LambdaQueryWrapper<InventoryCheckItem> iciW = new LambdaQueryWrapper<>();
        iciW.eq(InventoryCheckItem::getProductId, id);
        inventoryCheckItemMapper.delete(iciW);

        LambdaQueryWrapper<ProductStock> psW = new LambdaQueryWrapper<>();
        psW.eq(ProductStock::getProductId, id);
        productStockMapper.delete(psW);

        productService.removeById(id);
        return Result.success();
    }
}