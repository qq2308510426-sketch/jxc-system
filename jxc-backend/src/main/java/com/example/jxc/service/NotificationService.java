package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.entity.Notification;

public interface NotificationService extends IService<Notification> {

    void createNotification(String type, String title, String content, Long relatedId, Long userId);

    void checkStockWarnings();

    /** 扫描临期与已过效期的在库批次，生成站内通知（7 天内到期视为临期）。 */
    void checkBatchExpiry();

    Page<Notification> listPage(int pageNum, int pageSize, Long userId, Integer isRead);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);

    int getUnreadCount(Long userId);
}