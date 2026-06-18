package com.example.jxc.service;

import com.example.jxc.vo.ReportVO;

import java.util.List;
import java.util.Map;

public interface ReportService {

    List<ReportVO> salesReport(String startDate, String endDate);

    List<ReportVO> stockReport();
    List<Map<String, Object>> getCustomerReconciliation();
    List<Map<String, Object>> getSupplierReconciliation();
}
