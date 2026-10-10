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
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.AccountReceivable;
import com.example.jxc.mapper.AccountReceivableMapper;
import com.example.jxc.service.StockService;
import com.example.jxc.mapper.OrderMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.CustomerMapper;
import com.example.jxc.entity.Customer;
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
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private AccountReceivableMapper accountReceivableMapper;

    @Autowired
    private StockService stockService;

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + dateStr + uuid;
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
                throw new BusinessException("\u5ba2\u6237\u4fe1\u7528\u989d\u5ea6\u4e0d\u8db3\uff0c\u5f53\u524d\u5e94\u4ed8\u6b3e: " + outstandingAmount + " / \u989d\u5ea6: " + customer.getCreditLimit());
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

            product.setReservedStock((product.getReservedStock() != null ? product.getReservedStock() : 0) + itemDTO.getQuantity());
            if (productMapper.updateById(product) == 0) {
                throw new BusinessException("库存已被其他操作修改，请重试");
            }
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, int status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }

        // 取消必须连同预留库存一起回滚，统一交给 cancelOrder 处理
        if (status == 4) {
            cancelOrder(orderId, null);
            return;
        }

        int from = order.getStatus();
        boolean allowed = (from == 0 && status == 1)
                || (from == 2 && status == 3);
        if (!allowed) {
            throw new BusinessException("\u975e\u6cd5\u7684\u8ba2\u5355\u72b6\u6001\u6d41\u8f6c\uff1a" + from + " -> " + status
                    + "\uff1b\u53d6\u6d88\u8ba2\u5355\u8bf7\u4f7f\u7528\u53d6\u6d88\u529f\u80fd\uff0c\u5df2\u53d1\u8d27\u8ba2\u5355\u9700\u8d70\u9000\u8d27\u6d41\u7a0b");
        }

        order.setStatus(status);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId, String shippingNo, String shippingCompany, Long operatorId) {
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
            StockOperateDTO out = new StockOperateDTO();
            out.setProductId(item.getProductId());
            out.setQuantity(item.getQuantity());
            out.setUnitPrice(item.getUnitPrice());
            out.setCustomerId(order.getCustomerId());
            out.setOrderId(order.getId());
            // 出库流水、总库存、分仓台账与成本结转统一由库存服务处理，避免两套口径
            stockService.stockOut(out, operatorId);

            Product product = productMapper.selectById(item.getProductId());
            int currentReserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
            product.setReservedStock(Math.max(0, currentReserved - item.getQuantity()));
            if (productMapper.updateById(product) == 0) {
                throw new BusinessException("\u5e93\u5b58\u5df2\u88ab\u5176\u4ed6\u64cd\u4f5c\u4fee\u6539\uff0c\u8bf7\u91cd\u8bd5");
            }
        }

        order.setShippingNo(shippingNo);
        order.setShippingCompany(shippingCompany);
        order.setShippingStatus(1);
        order.setShippingTime(LocalDateTime.now());
        order.setStatus(2);
        orderMapper.updateById(order);

        AccountReceivable receivable = new AccountReceivable();
        receivable.setCustomerId(order.getCustomerId());
        receivable.setOrderId(order.getId());
        receivable.setAmount(order.getTotalAmount());
        receivable.setPaidAmount(BigDecimal.ZERO);
        receivable.setStatus(0);
        receivable.setDueDate(LocalDate.now().plusDays(30));
        receivable.setRemark("\u53d1\u8d27\u81ea\u52a8\u751f\u6210\uff1a" + order.getOrderNo());
        accountReceivableMapper.insert(receivable);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u8ba2\u5355\u4e0d\u5b58\u5728");
        }
        if (order.getStatus() == 4) {
            throw new BusinessException("\u8ba2\u5355\u5df2\u53d6\u6d88");
        }
        if (order.getStatus() >= 2) {
            throw new BusinessException("\u5df2\u53d1\u8d27\u7684\u8ba2\u5355\u4e0d\u80fd\u53d6\u6d88\uff0c\u8bf7\u8d70\u9000\u8d27\u6d41\u7a0b");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                int currentReserved = product.getReservedStock() != null ? product.getReservedStock() : 0;
                product.setReservedStock(Math.max(0, currentReserved - item.getQuantity()));
                if (productMapper.updateById(product) == 0) {
                    throw new BusinessException("\u5e93\u5b58\u5df2\u88ab\u5176\u4ed6\u64cd\u4f5c\u4fee\u6539\uff0c\u8bf7\u91cd\u8bd5");
                }
            }
        }

        order.setStatus(4);
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
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
        }
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