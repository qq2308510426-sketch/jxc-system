package com.example.jxc.controller;

import com.example.jxc.common.Result;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.Customer;
import com.example.jxc.entity.Supplier;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.service.ProductService;
import com.example.jxc.service.CustomerService;
import com.example.jxc.service.SupplierService;
import com.example.jxc.util.CsvExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/data")
public class DataImportExportController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "text/csv", "text/plain", "application/vnd.ms-excel",
        "application/csv", "application/octet-stream"
    );

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("\u6587\u4ef6\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("\u6587\u4ef6\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc75MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BusinessException("\u4ec5\u652f\u6301CSV\u6587\u4ef6\u683c\u5f0f");
        }
    }

    // ==================== Export ====================

    @GetMapping("/export/products")
    public void exportProducts(HttpServletResponse response) throws Exception {
        List<Product> products = productService.list();
        String[] headers = {"ID", "\u5546\u54c1\u540d\u79f0", "\u89c4\u683c", "\u5355\u4ef7", "\u5e93\u5b58", "\u5b89\u5168\u5e93\u5b58", "\u72b6\u6001"};
        List<String[]> rows = new ArrayList<>();
        for (Product p : products) {
            rows.add(new String[]{
                String.valueOf(p.getId()),
                p.getName(),
                p.getSpec(),
                p.getPrice() != null ? p.getPrice().toPlainString() : "0",
                String.valueOf(p.getStock()),
                String.valueOf(p.getSafetyStock()),
                p.getStatus() == 1 ? "\u542f\u7528" : "\u7981\u7528"
            });
        }
        CsvExportUtil.export(response, "products", headers, rows);
    }

    @GetMapping("/export/customers")
    public void exportCustomers(HttpServletResponse response) throws Exception {
        List<Customer> customers = customerService.list();
        String[] headers = {"ID", "\u5ba2\u6237\u540d\u79f0", "\u8054\u7cfb\u4eba", "\u7535\u8bdd", "\u5730\u5740", "\u4fe1\u7528\u7b49\u7ea7"};
        List<String[]> rows = new ArrayList<>();
        for (Customer c : customers) {
            rows.add(new String[]{
                String.valueOf(c.getId()),
                c.getName(),
                c.getContact(),
                c.getPhone(),
                c.getAddress(),
                c.getCreditLevel()
            });
        }
        CsvExportUtil.export(response, "customers", headers, rows);
    }

    @GetMapping("/export/suppliers")
    public void exportSuppliers(HttpServletResponse response) throws Exception {
        List<Supplier> suppliers = supplierService.list();
        String[] headers = {"ID", "\u4f9b\u5e94\u5546\u540d\u79f0", "\u8054\u7cfb\u4eba", "\u7535\u8bdd", "\u5730\u5740"};
        List<String[]> rows = new ArrayList<>();
        for (Supplier s : suppliers) {
            rows.add(new String[]{
                String.valueOf(s.getId()),
                s.getName(),
                s.getContact(),
                s.getPhone(),
                s.getAddress()
            });
        }
        CsvExportUtil.export(response, "suppliers", headers, rows);
    }

    // ==================== Import ====================

    @PostMapping("/import/products")
    public Result<String> importProducts(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        try {
            List<Product> products = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (firstLine) { firstLine = false; continue; }
                if (lineNum > 10001) {
                    throw new BusinessException("\u5355\u6b21\u5bfc\u5165\u4e0d\u80fd\u8d85\u8fc710000\u6761\u6570\u636e");
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Product p = new Product();
                    p.setName(parts[1].trim().substring(0, Math.min(parts[1].trim().length(), 200)));
                    p.setSpec(parts[2].trim().substring(0, Math.min(parts[2].trim().length(), 200)));
                    p.setPrice(new BigDecimal(parts[3].trim()));
                    p.setStock(Integer.parseInt(parts[4].trim()));
                    if (parts.length > 5) p.setSafetyStock(Integer.parseInt(parts[5].trim()));
                    p.setStatus(1);
                    products.add(p);
                }
            }
            productService.saveBatch(products);
            return Result.success("\u5bfc\u5165\u6210\u529f\uff0c\u5171\u5bfc\u5165 " + products.size() + " \u6761\u6570\u636e");
        } catch (BusinessException e) {
            throw e;
        } catch (NumberFormatException e) {
            return Result.error("\u5bfc\u5165\u5931\u8d25: \u6570\u5b57\u683c\u5f0f\u9519\u8bef - " + e.getMessage());
        } catch (Exception e) {
            return Result.error("\u5bfc\u5165\u5931\u8d25: " + e.getMessage());
        }
    }

    @PostMapping("/import/customers")
    public Result<String> importCustomers(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        try {
            List<Customer> customers = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (firstLine) { firstLine = false; continue; }
                if (lineNum > 10001) {
                    throw new BusinessException("\u5355\u6b21\u5bfc\u5165\u4e0d\u80fd\u8d85\u8fc710000\u6761\u6570\u636e");
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Customer c = new Customer();
                    c.setName(parts[1].trim().substring(0, Math.min(parts[1].trim().length(), 100)));
                    c.setContact(parts[2].trim().substring(0, Math.min(parts[2].trim().length(), 50)));
                    if (parts.length > 3) c.setPhone(parts[3].trim().substring(0, Math.min(parts[3].trim().length(), 20)));
                    if (parts.length > 4) c.setAddress(parts[4].trim().substring(0, Math.min(parts[4].trim().length(), 255)));
                    c.setStatus(1);
                    customers.add(c);
                }
            }
            customerService.saveBatch(customers);
            return Result.success("\u5bfc\u5165\u6210\u529f\uff0c\u5171\u5bfc\u5165 " + customers.size() + " \u6761\u6570\u636e");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("\u5bfc\u5165\u5931\u8d25: " + e.getMessage());
        }
    }

    @PostMapping("/import/suppliers")
    public Result<String> importSuppliers(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        try {
            List<Supplier> suppliers = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (firstLine) { firstLine = false; continue; }
                if (lineNum > 10001) {
                    throw new BusinessException("\u5355\u6b21\u5bfc\u5165\u4e0d\u80fd\u8d85\u8fc710000\u6761\u6570\u636e");
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Supplier s = new Supplier();
                    s.setName(parts[1].trim().substring(0, Math.min(parts[1].trim().length(), 100)));
                    s.setContact(parts[2].trim().substring(0, Math.min(parts[2].trim().length(), 50)));
                    if (parts.length > 3) s.setPhone(parts[3].trim().substring(0, Math.min(parts[3].trim().length(), 20)));
                    if (parts.length > 4) s.setAddress(parts[4].trim().substring(0, Math.min(parts[4].trim().length(), 255)));
                    s.setStatus(1);
                    suppliers.add(s);
                }
            }
            supplierService.saveBatch(suppliers);
            return Result.success("\u5bfc\u5165\u6210\u529f\uff0c\u5171\u5bfc\u5165 " + suppliers.size() + " \u6761\u6570\u636e");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("\u5bfc\u5165\u5931\u8d25: " + e.getMessage());
        }
    }
}