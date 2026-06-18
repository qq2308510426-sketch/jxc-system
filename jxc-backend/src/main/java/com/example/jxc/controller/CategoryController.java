package com.example.jxc.controller;

import com.example.jxc.common.Result;
import com.example.jxc.entity.Category;
import com.example.jxc.entity.Product;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jxc.service.CategoryService;
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
        return Result.success(categoryService.getTree());
    }

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Category::getParentId, id);
        if (categoryService.count(childWrapper) > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        LambdaQueryWrapper<Product> prodWrapper = new LambdaQueryWrapper<>();
        prodWrapper.eq(Product::getCategoryId, id);
        if (productMapper.selectCount(prodWrapper) > 0) {
            throw new BusinessException("该分类下有商品，无法删除");
        }
        categoryService.removeById(id);
        return Result.success();
    }
}