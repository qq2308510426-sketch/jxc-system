package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Log;
import com.example.jxc.mapper.LogMapper;
import com.example.jxc.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public Page<Log> listPage(int pageNum, int pageSize) {
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Log::getCreateTime);
        return logMapper.selectPage(page, wrapper);
    }
}
