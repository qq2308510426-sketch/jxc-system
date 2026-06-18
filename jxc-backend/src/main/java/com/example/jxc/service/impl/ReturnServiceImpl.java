package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;
import com.example.jxc.entity.StockIn;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.ReturnItemMapper;
import com.example.jxc.mapper.ReturnMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReturnServiceImpl extends ServiceImpl<ReturnMapper, Return> implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    @Autowired
    private ReturnItemMapper returnItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Override
    @Transactional
    public void createReturn(Return returnObj, List<ReturnItem> items) {
        String returnNo = "RET" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", (int)(Math.random() * 10000));
        returnObj.setReturnNo(returnNo);
        returnObj.setStatus(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnItem item : items) {
            totalAmount = totalAmount.add(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
        }
        returnObj.setTotalAmount(totalAmount);
        returnMapper.insert(returnObj);

        for (ReturnItem item : items) {
            item.setReturnId(returnObj.getId());
            returnItemMapper.insert(item);
        }
    }

    @Override
    @Transactional
    public void processReturn(Long returnId, int status, Long operatorId) {
        Return returnObj = returnMapper.selectById(returnId);
        if (returnObj == null) {
            throw new BusinessException("\u9000\u8d27\u5355\u4e0d\u5b58\u5728");
        }
        if (returnObj.getStatus() != 0) {
            throw new BusinessException("\u9000\u8d27\u5355\u5df2\u5904\u7406");
        }

        if (status == 1) {
            LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReturnItem::getReturnId, returnId);
            List<ReturnItem> items = returnItemMapper.selectList(wrapper);

            for (ReturnItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productMapper.updateById(product);

                    StockIn stockIn = new StockIn();
                    stockIn.setProductId(item.getProductId());
                    stockIn.setQuantity(item.getQuantity());
                    stockIn.setUnitPrice(item.getUnitPrice());
                    stockIn.setSupplierId(null);
                    stockIn.setOperatorId(operatorId);
                    stockInMapper.insert(stockIn);
                }
            }
        }

        returnObj.setStatus(status);
        returnMapper.updateById(returnObj);
    }

    @Override
    public Page<Return> listPage(int pageNum, int pageSize, Integer status) {
        Page<Return> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Return> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Return::getStatus, status);
        }
        wrapper.orderByDesc(Return::getCreateTime);
        return returnMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ReturnItem> getReturnItems(Long returnId) {
        LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnItem::getReturnId, returnId);
        return returnItemMapper.selectList(wrapper);
    }
}