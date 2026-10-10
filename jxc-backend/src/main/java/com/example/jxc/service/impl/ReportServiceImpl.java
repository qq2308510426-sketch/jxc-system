package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.entity.*;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.*;
import com.example.jxc.service.ReportService;
import com.example.jxc.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    @Autowired private StockOutMapper stockOutMapper;
    @Autowired private AccountReceivableMapper receivableMapper;
    @Autowired private AccountPayableMapper payableMapper;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static LocalDate parseDate(String value, String label) {
        if (value == null || value.isEmpty()) {
            throw new BusinessException(label + "不能为空");
        }
        try {
            return LocalDate.parse(value, DAY);
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + "格式应为 yyyy-MM-dd: " + value);
        }
    }

    /**
     * 销售报表以实际出库流水为确认口径：发货当天确认销售额，并按出库时点结转的成本计算毛利。
     * 未发货的订单不计入收入，历史出库单据若未记录结转成本则用商品当前加权成本兜底。
     */
    @Override
    public List<ReportVO> salesReport(String startDate, String endDate) {
        LocalDate start = parseDate(startDate, "开始日期");
        LocalDate end = parseDate(endDate, "结束日期");
        if (start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (start.plusDays(366).isBefore(end)) {
            throw new BusinessException("统计区间不能超过一年");
        }

        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<StockOut> sw = new LambdaQueryWrapper<>();
        sw.ge(StockOut::getCreateTime, rangeStart)
          .lt(StockOut::getCreateTime, rangeEnd);
        List<StockOut> outs = stockOutMapper.selectList(sw);

        Set<Long> fallbackProductIds = outs.stream()
            .filter(o -> o.getCostPrice() == null)
            .map(StockOut::getProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, BigDecimal> currentCost = new HashMap<>();
        if (!fallbackProductIds.isEmpty()) {
            LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
            pw.in(Product::getId, fallbackProductIds).select(Product::getId, Product::getCostPrice);
            for (Product p : productMapper.selectList(pw)) {
                if (p.getCostPrice() != null) {
                    currentCost.put(p.getId(), p.getCostPrice());
                }
            }
        }

        Map<LocalDate, List<StockOut>> byDay = outs.stream()
            .filter(o -> o.getCreateTime() != null)
            .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));

        List<ReportVO> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            ReportVO vo = new ReportVO();
            vo.setPeriod(d.format(DAY));

            List<StockOut> dayOuts = byDay.getOrDefault(d, Collections.emptyList());
            vo.setOrderCount((int) dayOuts.stream().map(StockOut::getOrderId)
                .filter(Objects::nonNull).distinct().count());

            BigDecimal totalSales = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            for (StockOut o : dayOuts) {
                int qty = o.getQuantity() == null ? 0 : o.getQuantity();
                if (o.getUnitPrice() != null) {
                    totalSales = totalSales.add(o.getUnitPrice().multiply(BigDecimal.valueOf(qty)));
                }
                BigDecimal unitCost = o.getCostPrice() != null
                    ? o.getCostPrice()
                    : currentCost.getOrDefault(o.getProductId(), BigDecimal.ZERO);
                totalCost = totalCost.add(unitCost.multiply(BigDecimal.valueOf(qty)));
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
            int stock = p.getStock() == null ? 0 : p.getStock();
            int safety = p.getSafetyStock() == null ? 0 : p.getSafetyStock();
            vo.setWarning(stock <= safety);
            result.add(vo);
        }
        return result;
    }

    /** 客户对账：直接读取应收台账，与「应收应付」页面同一数据源。 */
    @Override
    public List<Map<String, Object>> getCustomerReconciliation() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Customer> customers = customerMapper.selectList(null);
        if (customers.isEmpty()) return result;

        Set<Long> customerIds = customers.stream().map(Customer::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<AccountReceivable> rw = new LambdaQueryWrapper<>();
        rw.in(AccountReceivable::getCustomerId, customerIds);
        Map<Long, List<AccountReceivable>> byCustomer = receivableMapper.selectList(rw).stream()
            .collect(Collectors.groupingBy(AccountReceivable::getCustomerId));

        LocalDate today = LocalDate.now();
        for (Customer c : customers) {
            List<AccountReceivable> list = byCustomer.getOrDefault(c.getId(), Collections.emptyList());
            if (list.isEmpty()) continue;

            BigDecimal total = list.stream().map(AccountReceivable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal received = list.stream().map(AccountReceivable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long overdue = list.stream().filter(r -> r.getStatus() != 2
                && r.getDueDate() != null && r.getDueDate().isBefore(today)).count();

            Map<String, Object> item = new HashMap<>();
            item.put("customerId", c.getId());
            item.put("customerName", c.getName());
            item.put("totalOrders", list.size());
            item.put("totalAmount", total);
            item.put("receivedAmount", received);
            item.put("outstanding", total.subtract(received));
            item.put("overdueCount", overdue);
            result.add(item);
        }
        return result;
    }

    /** 供应商对账：直接读取应付台账，入库流水只作为业务明细来源。 */
    @Override
    public List<Map<String, Object>> getSupplierReconciliation() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Supplier> suppliers = supplierMapper.selectList(null);
        if (suppliers.isEmpty()) return result;

        Set<Long> supplierIds = suppliers.stream().map(Supplier::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<AccountPayable> pw = new LambdaQueryWrapper<>();
        pw.in(AccountPayable::getSupplierId, supplierIds);
        Map<Long, List<AccountPayable>> bySupplier = payableMapper.selectList(pw).stream()
            .collect(Collectors.groupingBy(AccountPayable::getSupplierId));

        LambdaQueryWrapper<StockIn> sw = new LambdaQueryWrapper<>();
        sw.in(StockIn::getSupplierId, supplierIds);
        Map<Long, List<StockIn>> insBySupplier = stockInMapper.selectList(sw).stream()
            .filter(si -> si.getSupplierId() != null)
            .collect(Collectors.groupingBy(StockIn::getSupplierId));

        LocalDate today = LocalDate.now();
        for (Supplier s : suppliers) {
            List<AccountPayable> payables = bySupplier.getOrDefault(s.getId(), Collections.emptyList());
            List<StockIn> stockIns = insBySupplier.getOrDefault(s.getId(), Collections.emptyList());
            if (payables.isEmpty() && stockIns.isEmpty()) continue;

            BigDecimal total = payables.stream().map(AccountPayable::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paid = payables.stream().map(AccountPayable::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long overdue = payables.stream().filter(p -> p.getStatus() != 2
                && p.getDueDate() != null && p.getDueDate().isBefore(today)).count();
            BigDecimal inboundAmount = stockIns.stream()
                .filter(si -> si.getUnitPrice() != null && si.getQuantity() != null)
                .map(si -> si.getUnitPrice().multiply(BigDecimal.valueOf(si.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("supplierId", s.getId());
            item.put("supplierName", s.getName());
            item.put("totalOrders", payables.size());
            item.put("totalAmount", total);
            item.put("paidAmount", paid);
            item.put("outstanding", total.subtract(paid));
            item.put("inboundAmount", inboundAmount);
            item.put("overdueCount", overdue);
            result.add(item);
        }
        return result;
    }
}
