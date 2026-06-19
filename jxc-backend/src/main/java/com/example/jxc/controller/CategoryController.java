package com.example.jxc.controller;

import com.example.jxc.aspect.OperationLog;
import com.example.jxc.common.Result;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.entity.Category;
import com.example.jxc.entity.Product;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        return Result.success(categoryService.list());
    }

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @OperationLog(action = "\u65b0\u589e\u5206\u7c7b")
    @PostMapping
    public Result<Void> create(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @OperationLog(action = "\u4fee\u6539\u5206\u7c7b")
    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @OperationLog(action = "\u5220\u9664\u5206\u7c7b")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, id);
        if (productMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("\u8be5\u5206\u7c7b\u4e0b\u6709\u5173\u8054\u5546\u54c1\uff0c\u65e0\u6cd5\u5220\u9664");
        }
        categoryService.removeById(id);
        return Result.success();
    }
}
