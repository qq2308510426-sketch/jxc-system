package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.InventoryCheck;
import com.example.jxc.entity.InventoryCheckItem;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.InventoryCheckItemMapper;
import com.example.jxc.mapper.InventoryCheckMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.service.InventoryCheckService;
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

    @Override
    @Transactional
    public void createCheck(List<InventoryCheckItem> items, Long operatorId) {
        String checkNo = "CHK" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", (int)(Math.random() * 10000));

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

        for (InventoryCheckItem item : items) {
            if (item.getDifference() != null && item.getDifference() != 0) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(item.getActualStock());
                    productMapper.updateById(product);

                    if (item.getDifference() > 0) {
                        StockOut stockOut = new StockOut();
                        stockOut.setProductId(item.getProductId());
                        stockOut.setQuantity(item.getDifference());
                        stockOut.setUnitPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
                        stockOut.setOperatorId(operatorId);
                        stockOutMapper.insert(stockOut);
                    } else {
                        StockIn stockIn = new StockIn();
                        stockIn.setProductId(item.getProductId());
                        stockIn.setQuantity(Math.abs(item.getDifference()));
                        stockIn.setUnitPrice(product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO);
                        stockIn.setOperatorId(operatorId);
                        stockInMapper.insert(stockIn);
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