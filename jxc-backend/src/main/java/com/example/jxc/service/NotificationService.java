package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Notification;

public interface NotificationService extends IService<Notification> {

    void createNotification(String type, String title, String content, Long relatedId, Long userId);

    void checkStockWarnings();

    Page<Notification> listPage(int pageNum, int pageSize, Long userId, Integer isRead);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);

    int getUnreadCount(Long userId);
}