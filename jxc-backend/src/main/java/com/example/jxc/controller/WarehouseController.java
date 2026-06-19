package com.example.jxc.controller;

import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.Result;
import com.example.jxc.entity.Warehouse;
import com.example.jxc.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        return Result.success(warehouseService.list());
    }

    @GetMapping("/all")
    public Result<List<Warehouse>> all() {
        return Result.success(warehouseService.list());
    }

    @OperationLog(action = "\u65b0\u589e\u4ed3\u5e93")
    @PostMapping
    public Result<Void> create(@RequestBody Warehouse warehouse) {
        warehouse.setStatus(1);
        warehouseService.save(warehouse);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u4ed3\u5e93")
    @PutMapping
    public Result<Void> update(@RequestBody Warehouse warehouse) {
        warehouseService.updateById(warehouse);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u4ed3\u5e93")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.success();
    }
}