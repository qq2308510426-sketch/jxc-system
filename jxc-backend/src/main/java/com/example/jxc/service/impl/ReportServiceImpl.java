package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.entity.*;
import com.example.jxc.mapper.*;
import com.example.jxc.service.ReportService;
import com.example.jxc.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private StockInMapper stockInMapper;

    @Override
    public List<ReportVO> salesReport(String startDate, String endDate) {
        List<ReportVO> result = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            ReportVO vo = new ReportVO();
            vo.setPeriod(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
            w.ge(Order::getCreateTime, d.atStartOfDay()).lt(Order::getCreateTime, d.plusDays(1).atStartOfDay()).in(Order::getStatus, 1, 2, 3);
            List<Order> orders = orderMapper.selectList(w);
            vo.setOrderCount(orders.size());
            BigDecimal totalSales = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = BigDecimal.ZERO;
            for (Order order : orders) {
                LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
                iw.eq(OrderItem::getOrderId, order.getId());
                List<OrderItem> items = orderItemMapper.selectList(iw);
                for (OrderItem item : items) {
                    Product product = productMapper.selectById(item.getProductId());
                    if (product != null && product.getCostPrice() != null) {
                        totalCost = totalCost.add(product.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            vo.setTotalSales(totalSales);
            vo.setTotalCost(totalCost);
            vo.setProfit(totalSales.subtract(totalCost));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ReportVO> stockReport() {
        List<ReportVO> result = new ArrayList<>();
        for (Product p : productMapper.selectList(null)) {
            ReportVO vo = new ReportVO();
            vo.setProductId(p.getId());
            vo.setProductName(p.getName());
            vo.setCurrentStock(p.getStock());
            vo.setSafetyStock(p.getSafetyStock());
            vo.setWarning(p.getStock() <= p.getSafetyStock());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getCustomerReconciliation() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Customer c : customerMapper.selectList(null)) {
            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getId());
            item.put("customerName", c.getName());
            LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
            w.eq(Order::getCustomerId, c.getId());
            List<Order> orders = orderMapper.selectList(w);
            item.put("totalOrders", orders.size());
            item.put("totalAmount", orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSupplierReconciliation() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Supplier s : supplierMapper.selectList(null)) {
            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", s.getId());
            item.put("supplierName", s.getName());
            LambdaQueryWrapper<StockIn> w = new LambdaQueryWrapper<>();
            w.eq(StockIn::getSupplierId, s.getId());
            List<StockIn> stockIns = stockInMapper.selectList(w);
            item.put("totalOrders", stockIns.size());
            BigDecimal total = stockIns.stream()
                .filter(si -> si.getUnitPrice() != null && si.getQuantity() != null)
                .map(si -> si.getUnitPrice().multiply(BigDecimal.valueOf(si.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("totalAmount", total);
            result.add(item);
        }
        return result;
    }
}