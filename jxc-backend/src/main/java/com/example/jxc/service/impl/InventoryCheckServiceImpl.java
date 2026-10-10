package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.InventoryCheck;
import com.example.jxc.entity.InventoryCheckItem;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.ProductStock;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.InventoryCheckItemMapper;
import com.example.jxc.mapper.InventoryCheckMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.ProductStockMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.service.InventoryCheckService;
import com.example.jxc.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InventoryCheckServiceImpl extends ServiceImpl<InventoryCheckMapper, InventoryCheck> implements InventoryCheckService {

    @Autowired
    private InventoryCheckMapper checkMapper;

    @Autowired
    private InventoryCheckItemMapper checkItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Override
    @Transactional
    public void createCheck(List<InventoryCheckItem> items, Long operatorId) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("盘点明细不能为空");
        }
        String checkNo = "CHK" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        InventoryCheck check = new InventoryCheck();
        check.setCheckNo(checkNo);
        check.setStatus(0);
        check.setOperatorId(operatorId);
        checkMapper.insert(check);

        for (InventoryCheckItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728: " + item.getProductId());
            }

            item.setCheckId(check.getId());
            item.setSystemStock(product.getStock());
            if (item.getActualStock() == null) {
                throw new BusinessException("实盘数量不能为空: " + product.getName());
            }
            item.setDifference(item.getSystemStock() - item.getActualStock());
            checkItemMapper.insert(item);
        }
    }

    @Override
    @Transactional
    public void completeCheck(Long checkId, Long operatorId) {
        InventoryCheck check = checkMapper.selectById(checkId);
        if (check == null) {
            throw new BusinessException("\u76d8\u70b9\u5355\u4e0d\u5b58\u5728");
        }
        if (check.getStatus() == 1) {
            throw new BusinessException("\u76d8\u70b9\u5355\u5df2\u5b8c\u6210");
        }

        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, checkId);
        List<InventoryCheckItem> items = checkItemMapper.selectList(wrapper);

        Long operatorWarehouse = stockService.resolveWarehouseId(operatorId);

        for (InventoryCheckItem item : items) {
            if (item.getDifference() != null && item.getDifference() != 0) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    // 操作人未绑仓库时，商品只有一个台账仓库就记到那里
                    Long warehouseId = operatorWarehouse;
                    if (warehouseId == null) {
                        LambdaQueryWrapper<ProductStock> psw = new LambdaQueryWrapper<>();
                        psw.eq(ProductStock::getProductId, item.getProductId());
                        List<ProductStock> ledger = productStockMapper.selectList(psw);
                        if (ledger.size() == 1) {
                            warehouseId = ledger.get(0).getWarehouseId();
                        }
                    }
                    if (item.getDifference() > 0) {
                        StockOut stockOut = new StockOut();
                        stockOut.setProductId(item.getProductId());
                        stockOut.setQuantity(item.getDifference());
                        stockOut.setUnitPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
                        stockOut.setCostPrice(product.getCostPrice());
                        stockOut.setOperatorId(operatorId);
                        stockOut.setWarehouseId(warehouseId);
                        stockOutMapper.insert(stockOut);
                        stockService.adjustWarehouseStock(item.getProductId(), warehouseId, -item.getDifference());
                    } else {
                        StockIn stockIn = new StockIn();
                        stockIn.setProductId(item.getProductId());
                        stockIn.setQuantity(Math.abs(item.getDifference()));
                        stockIn.setUnitPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
                        stockIn.setOperatorId(operatorId);
                        stockIn.setWarehouseId(warehouseId);
                        stockInMapper.insert(stockIn);
                        stockService.adjustWarehouseStock(item.getProductId(), warehouseId, Math.abs(item.getDifference()));
                    }

                    product.setStock(item.getActualStock());
                    // 乐观锁更新：影响 0 行说明盘点期间库存已被其他单据改动
                    if (productMapper.updateById(product) == 0) {
                        throw new BusinessException("\u76d8\u70b9\u671f\u95f4\u5546\u54c1\u5e93\u5b58\u5df2\u88ab\u5176\u4ed6\u64cd\u4f5c\u4fee\u6539\uff0c\u8bf7\u91cd\u65b0\u76d8\u70b9");
                    }
                }
            }
        }

        check.setStatus(1);
        checkMapper.updateById(check);
    }

    @Override
    public Page<InventoryCheck> listPage(int pageNum, int pageSize) {
        Page<InventoryCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InventoryCheck::getCreateTime);
        return checkMapper.selectPage(page, wrapper);
    }

    @Override
    public List<InventoryCheckItem> getCheckItems(Long checkId) {
        LambdaQueryWrapper<InventoryCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryCheckItem::getCheckId, checkId);
        return checkItemMapper.selectList(wrapper);
    }
}