package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Notification;
import com.example.jxc.entity.Product;
import com.example.jxc.mapper.NotificationMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void createNotification(String type, String title, String content, Long relatedId, Long userId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setUserId(userId);
        notificationMapper.insert(notification);
    }

    @Override
    public void checkStockWarnings() {
        // Get all products and check stock levels
        List<Product> allProducts = productMapper.selectList(null);
        List<Product> lowStockProducts = new java.util.ArrayList<>();
        for (Product p : allProducts) {
            if (p.getSafetyStock() != null && p.getStock() <= p.getSafetyStock()) {
                lowStockProducts.add(p);
            }
        }

        for (Product product : lowStockProducts) {
            LambdaQueryWrapper<Notification> notifWrapper = new LambdaQueryWrapper<>();
            notifWrapper.eq(Notification::getType, "STOCK_LOW")
                       .eq(Notification::getRelatedId, product.getId())
                       .apply("DATE(create_time) = CURDATE()");
            if (notificationMapper.selectCount(notifWrapper) == 0) {
                createNotification(
                    "STOCK_LOW",
                    "\u5e93\u5b58\u9884\u8b66: " + product.getName(),
                    "\u5546\u54c1\u300c" + product.getName() + "\u300d\u5f53\u524d\u5e93\u5b58(" + product.getStock() + ")\u5df2\u4f4e\u4e8e\u5b89\u5168\u5e93\u5b58(" + product.getSafetyStock() + ")",
                    product.getId(),
                    null
                );
            }
        }
    }

    @Override
    public Page<Notification> listPage(int pageNum, int pageSize, Long userId, Integer isRead) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.and(w -> w.isNull(Notification::getUserId).or().eq(Notification::getUserId, userId));
        }
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        wrapper.orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(page, wrapper);
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 0);
        if (userId != null) {
            wrapper.and(w -> w.isNull(Notification::getUserId).or().eq(Notification::getUserId, userId));
        }

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        for (Notification n : notifications) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 0);
        if (userId != null) {
            wrapper.and(w -> w.isNull(Notification::getUserId).or().eq(Notification::getUserId, userId));
        }
        return notificationMapper.selectCount(wrapper).intValue();
    }
}