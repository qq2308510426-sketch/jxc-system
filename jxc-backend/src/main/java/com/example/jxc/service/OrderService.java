package com.example.jxc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jxc.dto.OrderCreateDTO;
import com.example.jxc.entity.Order;
import com.example.jxc.vo.OrderVO;

public interface OrderService extends IService<Order> {

    void createOrder(OrderCreateDTO dto, Long operatorId);

    OrderVO getOrderDetail(Long orderId);

    void updateStatus(Long orderId, int status);

    Page<Order> listPage(int pageNum, int pageSize, Integer status, Long customerId);

    List<Order> getPendingOrders(Long customerId);

    void shipOrder(Long orderId, String shippingNo, String shippingCompany);

    void cancelOrder(Long orderId, Long operatorId);
}