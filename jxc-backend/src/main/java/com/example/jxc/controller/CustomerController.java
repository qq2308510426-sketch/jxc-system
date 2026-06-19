package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.dto.CustomerDTO;
import com.example.jxc.entity.Customer;
import com.example.jxc.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<PageResult<Customer>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Customer::getName, keyword);
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        Page<Customer> result = customerService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }

    @OperationLog(action = "\u65b0\u589e\u5ba2\u6237")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setContact(dto.getContact());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setCreditLevel(dto.getCreditLevel());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setRemark(dto.getRemark());
        customer.setStatus(1);
        customerService.save(customer);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u5ba2\u6237")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setContact(dto.getContact());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setCreditLevel(dto.getCreditLevel());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setRemark(dto.getRemark());
        if (dto.getStatus() != null) customer.setStatus(dto.getStatus());
        customerService.updateById(customer);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u5ba2\u6237")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.success();
    }
}