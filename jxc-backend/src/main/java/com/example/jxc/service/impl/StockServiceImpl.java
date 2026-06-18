package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.ProductBatch;
import com.example.jxc.entity.ProductStock;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ProductBatchMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.ProductStockMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StockServiceImpl extends ServiceImpl<StockInMapper, StockIn> implements StockService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private ProductBatchMapper productBatchMapper;

    @Override
    @Transactional
    public void stockIn(StockOperateDTO dto, Long operatorId) {
        if (dto.getSupplierId() == null) {
            throw new BusinessException("\u4f9b\u5e94\u5546ID\u4e0d\u80fd\u4e3a\u7a7a");
        }

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        StockIn stockIn = new StockIn();
        stockIn.setProductId(dto.getProductId());
        stockIn.setSupplierId(dto.getSupplierId());
        stockIn.setQuantity(dto.getQuantity());
        stockIn.setUnitPrice(dto.getUnitPrice());
        stockIn.setOperatorId(operatorId);
        stockIn.setWarehouseId(dto.getWarehouseId());
        stockIn.setBatchNo(dto.getBatchNo());
        stockInMapper.insert(stockIn);

        int oldStock = product.getStock();
        int addQty = dto.getQuantity();
        BigDecimal addCost = dto.getUnitPrice();

        if (product.getCostPrice() == null || oldStock == 0) {
            product.setCostPrice(addCost);
        } else {
            BigDecimal totalValue = product.getCostPrice().multiply(BigDecimal.valueOf(oldStock))
                .add(addCost.multiply(BigDecimal.valueOf(addQty)));
            product.setCostPrice(totalValue.divide(BigDecimal.valueOf(oldStock + addQty), 2, RoundingMode.HALF_UP));
        }

        product.setStock(oldStock + addQty);
        productMapper.updateById(product);

        if (dto.getWarehouseId() != null) {
            updateProductStock(dto.getProductId(), dto.getWarehouseId(), addQty);
        }

        if (dto.getBatchNo() != null && !dto.getBatchNo().isEmpty()) {
            ProductBatch batch = new ProductBatch();
            batch.setProductId(dto.getProductId());
            batch.setBatchNo(dto.getBatchNo());
            batch.setSerialNo(dto.getSerialNo());
            batch.setQuantity(dto.getQuantity());
            batch.setSupplierId(dto.getSupplierId());
            batch.setPurchasePrice(dto.getUnitPrice());
            batch.setStatus(1);
            productBatchMapper.insert(batch);
        }
    }

    @Override
    @Transactional
    public void stockOut(StockOperateDTO dto, Long operatorId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3\uff0c\u5f53\u524d\u5e93\u5b58: " + product.getStock());
        }

        StockOut stockOut = new StockOut();
        stockOut.setProductId(dto.getProductId());
        stockOut.setCustomerId(dto.getCustomerId());
        stockOut.setOrderId(dto.getOrderId());
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setUnitPrice(dto.getUnitPrice());
        stockOut.setOperatorId(operatorId);
        stockOut.setWarehouseId(dto.getWarehouseId());
        stockOutMapper.insert(stockOut);

        product.setStock(product.getStock() - dto.getQuantity());
        productMapper.updateById(product);

        if (dto.getWarehouseId() != null) {
            updateProductStock(dto.getProductId(), dto.getWarehouseId(), -dto.getQuantity());
        }
    }

    private void updateProductStock(Long productId, Long warehouseId, int quantityChange) {
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId)
               .eq(ProductStock::getWarehouseId, warehouseId);
        ProductStock ps = productStockMapper.selectOne(wrapper);

        if (ps == null) {
            ps = new ProductStock();
            ps.setProductId(productId);
            ps.setWarehouseId(warehouseId);
            ps.setQuantity(Math.max(0, quantityChange));
            productStockMapper.insert(ps);
        } else {
            int newQty = ps.getQuantity() + quantityChange;
            ps.setQuantity(Math.max(0, newQty));
            productStockMapper.updateById(ps);
        }
    }

    @Override
    public Page<StockIn> listStockIn(int pageNum, int pageSize) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockIn::getCreateTime);
        return stockInMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<StockOut> listStockOut(int pageNum, int pageSize) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StockOut::getCreateTime);
        return stockOutMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void cancelStockIn(Long stockInId, Long operatorId) {
        StockIn stockIn = stockInMapper.selectById(stockInId);
        if (stockIn == null) {
            throw new BusinessException("\u5165\u5e93\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }

        Product product = productMapper.selectById(stockIn.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        if (product.getStock() < stockIn.getQuantity()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3\uff0c\u65e0\u6cd5\u64a4\u9500\u5165\u5e93");
        }

        product.setStock(product.getStock() - stockIn.getQuantity());
        productMapper.updateById(product);

        if (stockIn.getWarehouseId() != null) {
            updateProductStock(stockIn.getProductId(), stockIn.getWarehouseId(), -stockIn.getQuantity());
        }

        stockInMapper.deleteById(stockInId);
    }
}