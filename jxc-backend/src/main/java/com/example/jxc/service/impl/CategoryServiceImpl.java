package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Category;
import com.example.jxc.mapper.CategoryMapper;
import com.example.jxc.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getTree() {
        List<Category> allCategories = this.list();

        List<Category> topLevel = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .sorted(Comparator.comparingInt(c -> c.getSort() != null ? c.getSort() : 0))
                .collect(Collectors.toList());

        topLevel.forEach(parent -> parent.setChildren(findChildren(parent.getId(), allCategories)));

        return topLevel;
    }

    private List<Category> findChildren(Long parentId, List<Category> allCategories) {
        return allCategories.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .sorted(Comparator.comparingInt(c -> c.getSort() != null ? c.getSort() : 0))
                .peek(child -> child.setChildren(findChildren(child.getId(), allCategories)))
                .collect(Collectors.toList());
    }
}