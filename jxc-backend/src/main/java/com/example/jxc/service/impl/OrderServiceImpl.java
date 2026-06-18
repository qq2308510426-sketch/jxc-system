package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.dto.OrderCreateDTO;
import com.example.jxc.entity.Order;
import com.example.jxc.entity.OrderItem;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.OrderItemMapper;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.StockOut;
import com.example.jxc.mapper.OrderMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.CustomerMapper;
import com.example.jxc.entity.Customer;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.mapper.SupplierMapper;
import com.example.jxc.entity.Supplier;
import com.example.jxc.service.OrderService;
import com.example.jxc.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    private static final AtomicLong ORDER_SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = ORDER_SEQ.incrementAndGet() % 100000;
        return "ORD" + dateStr + String.format("%05d", seq);
    }

    @Override
    @Transactional
    public void createOrder(OrderCreateDTO dto, Long operatorId) {
        String orderNo = generateOrderNo();

        BigDecimal totalAmount = dto.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer != null && customer.getCreditLimit() != null && customer.getCreditLimit().compareTo(BigDecimal.ZERO) > 0) {
            LambdaQueryWrapper<Order> creditWrapper = new LambdaQueryWrapper<>();
            creditWrapper.eq(Order::getCustomerId, dto.getCustomerId())
                        .in(Order::getStatus, 0, 1, 2);
            List<Order> pendingOrders = orderMapper.selectList(creditWrapper);
            BigDecimal outstandingAmount = pendingOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(totalAmount);
            if (outstandingAmount.compareTo(customer.getCreditLimit()) > 0) {
                throw new BusinessException("\u5ba2\u6237\u4fe1\u7528\u989d\u5ea6\u4e0d\u8db3\uff0c\u5f53\u524d\u5e94\u4ed8: " + outstandingAmount + " / \u989d\u5ea6: " + customer.getCreditLimit());
            }
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setCustomerId(dto.getCustomerId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setPaymentStatus(0);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRemark(dto.getRemark());
        order.setOperatorId(operatorId);
        orderMapper.insert(order);

        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setSubtotal(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItemMapper.insert(item);

            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728: " + itemDTO.getProductId());
            }

            int availableStock = product.getStock() - (product.getReservedStock() != null ? product.getReservedStock() : 0);
            if (availableStock < itemDTO.getQuantity()) {
                throw new BusinessException("\u5546\u54c1\u5e93\u5b58\u4e0d\u8db3: " + product.getName()
                    + " \u53ef\u7528\u5e93\u5b58: " + availableStock);
            }

            int currentReserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
            product.setReservedStock(currentReserved + itemDTO.getQuantity());
            productMapper.updateById(product);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, int status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }

        int oldStatus = order.getStatus();
        if (oldStatus == status) return;
        if (oldStatus == 4) {
            throw new BusinessException("\u5df2\u53d6\u6d88\u7684\u8ba2\u5355\u4e0d\u80fd\u4fee\u6539");
        }
        if (oldStatus == 3) {
            throw new BusinessException("\u5df2\u5b8c\u6210\u7684\u8ba2\u5355\u4e0d\u80fd\u4fee\u6539");
        }
        if (oldStatus == 0 && status != 1) {
            throw new BusinessException("\u5f85\u5904\u7406\u8ba2\u5355\u53ea\u80fd\u786e\u8ba4");
        }
        if (oldStatus == 1 && status != 2 && status != 4) {
            throw new BusinessException("\u5df2\u786e\u8ba4\u8ba2\u5355\u53ea\u80fd\u53d1\u8d27\u6216\u53d6\u6d88");
        }
        if (oldStatus == 2 && status != 3 && status != 4) {
            throw new BusinessException("\u53d1\u8d27\u4e2d\u8ba2\u5355\u53ea\u80fd\u5b8c\u6210\u6216\u53d6\u6d88");
        }
        if (status < 0 || status > 4) {
            throw new BusinessException("\u65e0\u6548\u7684\u72b6\u6001\u503c");
        }

        if (status == 4 && oldStatus != 4) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (OrderItem item : items) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    int currentReserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
                    product.setReservedStock(Math.max(0, currentReserved - item.getQuantity()));
                    productMapper.updateById(product);
                }
                LambdaQueryWrapper<StockOut> soWrapper = new LambdaQueryWrapper<>();
                soWrapper.eq(StockOut::getOrderId, orderId);
                stockOutMapper.delete(soWrapper);
            }
        }

        order.setStatus(status);
        orderMapper.updateById(order);
        }

    @Override
    @Transactional
    public void shipOrder(Long orderId, String shippingNo, String shippingCompany) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("\u53ea\u6709\u5df2\u786e\u8ba4\u7684\u8ba2\u5355\u624d\u80fd\u53d1\u8d27");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728: " + item.getProductId());
            }

            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("\u5546\u54c1\u5e93\u5b58\u4e0d\u8db3: " + product.getName()
                    + " \u5f53\u524d\u5e93\u5b58: " + product.getStock());
            }

            product.setStock(product.getStock() - item.getQuantity());
            int currentReserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
            product.setReservedStock(Math.max(0, currentReserved - item.getQuantity()));

            if (product.getCostPrice() == null) {
                product.setCostPrice(item.getUnitPrice());
            } else {
                BigDecimal oldCost = product.getCostPrice();
                int oldStock = product.getStock();
                int newQty = item.getQuantity();
                BigDecimal newCost = item.getUnitPrice();
                BigDecimal totalValue = oldCost.multiply(BigDecimal.valueOf(oldStock))
                    .add(newCost.multiply(BigDecimal.valueOf(newQty)));
                product.setCostPrice(totalValue.divide(BigDecimal.valueOf(oldStock + newQty), 2, RoundingMode.HALF_UP));
            }

            productMapper.updateById(product);

            StockOut stockOut = new StockOut();
            stockOut.setProductId(item.getProductId());
            stockOut.setCustomerId(order.getCustomerId());
            stockOut.setOrderId(order.getId());
            stockOut.setQuantity(item.getQuantity());
            stockOut.setUnitPrice(item.getUnitPrice());
            stockOut.setOperatorId(order.getOperatorId());
            stockOutMapper.insert(stockOut);
        }

        order.setShippingNo(shippingNo);
        order.setShippingCompany(shippingCompany);
        order.setShippingStatus(1);
        order.setShippingTime(LocalDateTime.now());
        order.setStatus(2);
        orderMapper.updateById(order);
        }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setOperatorId(order.getOperatorId());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        vo.setShippingNo(order.getShippingNo());
        vo.setShippingCompany(order.getShippingCompany());
        vo.setShippingStatus(order.getShippingStatus());
        vo.setShippingTime(order.getShippingTime());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setPaidAmount(order.getPaidAmount());

        Customer customer = customerMapper.selectById(order.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getName());
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        vo.setItems(items);

        return vo;
    }

    @Override
    public Page<Order> listPage(int pageNum, int pageSize, Integer status, Long customerId) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        if (customerId != null) {
            wrapper.eq(Order::getCustomerId, customerId);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Order> getPendingOrders(Long customerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCustomerId, customerId)
               .in(Order::getStatus, 0, 1)
               .orderByDesc(Order::getCreateTime);
        return orderMapper.selectList(wrapper);
    }
}