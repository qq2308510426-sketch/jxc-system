package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.dto.PurchaseOrderCreateDTO;
import com.example.jxc.entity.*;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.*;
import com.example.jxc.service.PurchaseOrderService;
import com.example.jxc.vo.PurchaseOrderVO;
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
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Autowired private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private SupplierMapper supplierMapper;
    @Autowired private AccountPayableMapper accountPayableMapper;

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PO" + dateStr + uuid;
    }

    @Override
    @Transactional
    public void createPurchaseOrder(PurchaseOrderCreateDTO dto, Long operatorId) {
        Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new BusinessException("\u4f9b\u5e94\u5546\u4e0d\u5b58\u5728");
        }

        BigDecimal totalAmount = dto.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplierId(dto.getSupplierId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentStatus(0);
        order.setRemark(dto.getRemark());
        order.setOperatorId(operatorId);
        purchaseOrderMapper.insert(order);

        for (PurchaseOrderCreateDTO.PurchaseItemDTO itemDTO : dto.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setSubtotal(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            item.setReceivedQuantity(0);
            purchaseOrderItemMapper.insert(item);
        }

        // Create accounts payable record
        AccountPayable payable = new AccountPayable();
        payable.setSupplierId(dto.getSupplierId());
        payable.setPurchaseOrderId(order.getId());
        payable.setAmount(totalAmount);
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setStatus(0);
        payable.setDueDate(LocalDate.now().plusDays(30));
        accountPayableMapper.insert(payable);
    }

    @Override
    @Transactional
    public void approvePurchaseOrder(Long orderId, Long approverId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u91c7\u8d2d\u5355\u4e0d\u5b58\u5728");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("\u53ea\u6709\u5f85\u5ba1\u6279\u7684\u91c7\u8d2d\u5355\u624d\u80fd\u5ba1\u6279");
        }
        order.setStatus(1);
        order.setApproverId(approverId);
        order.setApproveTime(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void receivePurchaseOrder(Long orderId, Long operatorId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u91c7\u8d2d\u5355\u4e0d\u5b58\u5728");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("\u53ea\u6709\u5df2\u5ba1\u6279\u7684\u91c7\u8d2d\u5355\u624d\u80fd\u6536\u8d27");
        }

        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getPurchaseOrderId, orderId);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(itemWrapper);

        for (PurchaseOrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728: " + item.getProductId());
            }

            // Update product stock
            int oldStock = product.getStock();
            int addQty = item.getQuantity();

            if (product.getCostPrice() == null || oldStock == 0) {
                product.setCostPrice(item.getUnitPrice());
            } else {
                BigDecimal totalValue = product.getCostPrice().multiply(BigDecimal.valueOf(oldStock))
                    .add(item.getUnitPrice().multiply(BigDecimal.valueOf(addQty)));
                product.setCostPrice(totalValue.divide(BigDecimal.valueOf(oldStock + addQty), 2, RoundingMode.HALF_UP));
            }

            product.setStock(oldStock + addQty);
            productMapper.updateById(product);

            // Update received quantity
            item.setReceivedQuantity(item.getQuantity());
            purchaseOrderItemMapper.updateById(item);
        }

        order.setStatus(2);
        order.setReceiveTime(LocalDateTime.now());
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancelPurchaseOrder(Long orderId, Long operatorId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u91c7\u8d2d\u5355\u4e0d\u5b58\u5728");
        }
        if (order.getStatus() >= 2) {
            throw new BusinessException("\u5df2\u6536\u8d27\u7684\u91c7\u8d2d\u5355\u4e0d\u80fd\u53d6\u6d88");
        }

        order.setStatus(4);
        purchaseOrderMapper.updateById(order);

        // Cancel associated accounts payable
        LambdaQueryWrapper<AccountPayable> payWrapper = new LambdaQueryWrapper<>();
        payWrapper.eq(AccountPayable::getPurchaseOrderId, orderId);
        AccountPayable payable = accountPayableMapper.selectOne(payWrapper);
        if (payable != null && payable.getStatus() == 0) {
            accountPayableMapper.deleteById(payable.getId());
        }
    }

    @Override
    public PurchaseOrderVO getOrderDetail(Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("\u91c7\u8d2d\u5355\u4e0d\u5b58\u5728");
        }

        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setPaidAmount(order.getPaidAmount());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setRemark(order.getRemark());
        vo.setOperatorId(order.getOperatorId());
        vo.setApproverId(order.getApproverId());
        vo.setApproveTime(order.getApproveTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setCreateTime(order.getCreateTime());

        Supplier supplier = supplierMapper.selectById(order.getSupplierId());
        if (supplier != null) {
            vo.setSupplierName(supplier.getName());
        }

        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getPurchaseOrderId, orderId);
        vo.setItems(purchaseOrderItemMapper.selectList(itemWrapper));

        return vo;
    }

    @Override
    public Page<PurchaseOrder> listPage(int pageNum, int pageSize, Integer status, Long supplierId) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        return purchaseOrderMapper.selectPage(page, wrapper);
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "\u5f85\u5ba1\u6279";
            case 1 -> "\u5df2\u5ba1\u6279";
            case 2 -> "\u5df2\u6536\u8d27";
            case 3 -> "\u5df2\u5b8c\u6210";
            case 4 -> "\u5df2\u53d6\u6d88";
            default -> "\u672a\u77e5";
        };
    }
}