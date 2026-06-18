package com.example.jxc.controller;

import com.example.jxc.common.Result;
import com.example.jxc.service.ReportService;
import com.example.jxc.util.CsvExportUtil;
import com.example.jxc.vo.ReportVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/sales")
    public Result<List<ReportVO>> salesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(reportService.salesReport(startDate, endDate));
    }

    @GetMapping("/stock")
    public Result<List<ReportVO>> stockReport() {
        return Result.success(reportService.stockReport());
    }

    @GetMapping("/customer-reconciliation")
    public Result<List<Map<String, Object>>> customerReconciliation() {
        return Result.success(reportService.getCustomerReconciliation());
    }

    @GetMapping("/supplier-reconciliation")
    public Result<List<Map<String, Object>>> supplierReconciliation() {
        return Result.success(reportService.getSupplierReconciliation());
    }

    @GetMapping("/sales/export")
    public void salesExport(@RequestParam String startDate, @RequestParam String endDate, HttpServletResponse response) throws IOException {
        List<ReportVO> data = reportService.salesReport(startDate, endDate);
        String[] headers = {"Period", "Orders", "Sales", "Cost", "Profit"};
        List<String[]> rows = new ArrayList<>();
        for (ReportVO vo : data) {
            rows.add(new String[]{vo.getPeriod(), String.valueOf(vo.getOrderCount()), vo.getTotalSales().toPlainString(), "0", vo.getProfit().toPlainString()});
        }
        CsvExportUtil.export(response, "sales_report", headers, rows);
    }
}