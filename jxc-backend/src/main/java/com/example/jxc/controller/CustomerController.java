package com.example.jxc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.jxc.common.PageResult;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Customer;
import com.example.jxc.entity.Order;
import com.example.jxc.entity.StockOut;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.OrderMapper;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @GetMapping("/list")
    public Result<PageResult<Customer>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Customer> page = customerService.listPage(pageNum, pageSize, keyword);
        PageResult<Customer> pageResult = new PageResult<>(page.getTotal(), page.getRecords());
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.updateById(customer);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getCustomerId, id);
        if (orderMapper.selectCount(orderWrapper) > 0) {
            throw new BusinessException("该客户有关联订单，无法删除");
        }
        LambdaQueryWrapper<StockOut> stockOutWrapper = new LambdaQueryWrapper<>();
        stockOutWrapper.eq(StockOut::getCustomerId, id);
        if (stockOutMapper.selectCount(stockOutWrapper) > 0) {
            throw new BusinessException("该客户有关联出库记录，无法删除");
        }
        customerService.removeById(id);
        return Result.success();
    }
}