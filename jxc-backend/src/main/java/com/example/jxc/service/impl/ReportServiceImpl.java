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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

        // Bulk fetch all orders in range
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.plusDays(1).atStartOfDay();
        LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
        ow.ge(Order::getCreateTime, rangeStart)
          .lt(Order::getCreateTime, rangeEnd)
          .in(Order::getStatus, 1, 2, 3);
        List<Order> allOrders = orderMapper.selectList(ow);

        // Group orders by date
        Map<LocalDate, List<Order>> ordersByDate = allOrders.stream()
            .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));

        // Bulk fetch all order items for these orders
        Set<Long> orderIds = allOrders.stream().map(Order::getId).collect(Collectors.toSet());
        Map<Long, List<OrderItem>> itemsByOrderId = new HashMap<>();
        Map<Long, BigDecimal> costByProductId = new HashMap<>();

        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
            iw.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> allItems = orderItemMapper.selectList(iw);
            itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

            // Bulk fetch product cost prices
            Set<Long> productIds = allItems.stream().map(OrderItem::getProductId).collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
                pw.in(Product::getId, productIds);
                pw.select(Product::getId, Product::getCostPrice);
                List<Product> products = productMapper.selectList(pw);
                costByProductId = products.stream()
                    .filter(p -> p.getCostPrice() != null)
                    .collect(Collectors.toMap(Product::getId, Product::getCostPrice, (a, b) -> a));
            }
        }

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            ReportVO vo = new ReportVO();
            vo.setPeriod(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<Order> dayOrders = ordersByDate.getOrDefault(d, Collections.emptyList());
            vo.setOrderCount(dayOrders.size());

            BigDecimal totalSales = dayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = BigDecimal.ZERO;
            for (Order order : dayOrders) {
                List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), Collections.emptyList());
                for (OrderItem item : items) {
                    BigDecimal costPrice = costByProductId.get(item.getProductId());
                    if (costPrice != null) {
                        totalCost = totalCost.add(costPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
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
        List<Customer> customers = customerMapper.selectList(null);
        if (customers.isEmpty()) return result;

        Set<Long> customerIds = customers.stream().map(Customer::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
        ow.in(Order::getCustomerId, customerIds);
        ow.select(Order::getId, Order::getCustomerId, Order::getTotalAmount);
        List<Order> allOrders = orderMapper.selectList(ow);

        Map<Long, List<Order>> ordersByCustomer = allOrders.stream()
            .collect(Collectors.groupingBy(Order::getCustomerId));

        for (Customer c : customers) {
            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getId());
            item.put("customerName", c.getName());
            List<Order> orders = ordersByCustomer.getOrDefault(c.getId(), Collections.emptyList());
            item.put("totalOrders", orders.size());
            item.put("totalAmount", orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSupplierReconciliation() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Supplier> suppliers = supplierMapper.selectList(null);
        if (suppliers.isEmpty()) return result;

        Set<Long> supplierIds = suppliers.stream().map(Supplier::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<StockIn> sw = new LambdaQueryWrapper<>();
        sw.in(StockIn::getSupplierId, supplierIds);
        sw.select(StockIn::getId, StockIn::getSupplierId, StockIn::getUnitPrice, StockIn::getQuantity);
        List<StockIn> allStockIns = stockInMapper.selectList(sw);

        Map<Long, List<StockIn>> stockInsBySupplier = allStockIns.stream()
            .collect(Collectors.groupingBy(StockIn::getSupplierId));

        for (Supplier s : suppliers) {
            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", s.getId());
            item.put("supplierName", s.getName());
            List<StockIn> stockIns = stockInsBySupplier.getOrDefault(s.getId(), Collections.emptyList());
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