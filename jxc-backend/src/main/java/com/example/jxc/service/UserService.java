package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> login(String username, String password, String ip);

    void updateUser(User user);
    void changePassword(Long userId, String oldPassword, String newPassword);
}