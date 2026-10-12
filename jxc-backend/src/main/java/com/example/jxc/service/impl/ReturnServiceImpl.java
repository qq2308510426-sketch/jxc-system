package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.entity.Order;
import com.example.jxc.entity.PurchaseOrder;
import com.example.jxc.entity.Return;
import com.example.jxc.entity.ReturnItem;
import com.example.jxc.entity.AccountPayable;
import com.example.jxc.entity.AccountReceivable;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.mapper.AccountPayableMapper;
import com.example.jxc.mapper.OrderMapper;
import com.example.jxc.mapper.PurchaseOrderMapper;
import com.example.jxc.mapper.AccountReceivableMapper;
import com.example.jxc.service.StockService;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ReturnItemMapper;
import com.example.jxc.mapper.ReturnMapper;
import com.example.jxc.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
public class ReturnServiceImpl extends ServiceImpl<ReturnMapper, Return> implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    @Autowired
    private ReturnItemMapper returnItemMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private AccountPayableMapper payableMapper;

    @Autowired
    private AccountReceivableMapper receivableMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Override
    @Transactional
    public void createReturn(Return returnObj, List<ReturnItem> items) {
        String returnNo = "RET" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        returnObj.setReturnNo(returnNo);
        returnObj.setStatus(0);

        if (Integer.valueOf(3).equals(returnObj.getType()) && returnObj.getSupplierId() == null) {
            throw new BusinessException("\u91c7\u8d2d\u9000\u8d27\u5fc5\u987b\u9009\u62e9\u4f9b\u5e94\u5546");
        }
        if (returnObj.getType() == null || returnObj.getType() < 1 || returnObj.getType() > 3) {
            throw new BusinessException("\u9000\u8d27\u7c7b\u578b\u53ea\u80fd\u662f 1\u9000\u8d27 / 2\u6362\u8d27 / 3\u91c7\u8d2d\u9000\u8d27");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("\u9000\u8d27\u660e\u7ec6\u4e0d\u80fd\u4e3a\u7a7a");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnItem item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("\u9000\u8d27\u660e\u7ec6\u7684\u5546\u54c1\u4e0e\u6570\u91cf\u4e0d\u80fd\u4e3a\u7a7a");
            }
            BigDecimal price = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
            item.setSubtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        returnObj.setTotalAmount(totalAmount);
        returnMapper.insert(returnObj);

        for (ReturnItem item : items) {
            item.setReturnId(returnObj.getId());
            returnItemMapper.insert(item);
        }
    }

    @Override
    @Transactional
    public void processReturn(Long returnId, int status, Long operatorId) {
        Return returnObj = returnMapper.selectById(returnId);
        if (returnObj == null) {
            throw new BusinessException("\u9000\u8d27\u5355\u4e0d\u5b58\u5728");
        }
        if (returnObj.getStatus() != 0) {
            throw new BusinessException("\u9000\u8d27\u5df2\u5904\u7406");
        }

        if (status == 1) {
            LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReturnItem::getReturnId, returnId);
            List<ReturnItem> items = returnItemMapper.selectList(wrapper);

            if (Integer.valueOf(3).equals(returnObj.getType())) {
                // 采购退货：扣减库存、记出库流水，并冲减该供应商的未结应付
                BigDecimal returned = BigDecimal.ZERO;
                for (ReturnItem item : items) {
                    StockOperateDTO out = new StockOperateDTO();
                    out.setProductId(item.getProductId());
                    out.setQuantity(item.getQuantity());
                    out.setUnitPrice(item.getUnitPrice());
                    out.setSupplierId(returnObj.getSupplierId());
                    stockService.stockOut(out, operatorId);
                    returned = returned.add(item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                reducePayable(returnObj.getSupplierId(), returned);
            } else {
                // 销售退货：回补库存并同步分仓台账，按现行成本入账、不重算加权平均成本
                for (ReturnItem item : items) {
                    StockOperateDTO back = new StockOperateDTO();
                    back.setProductId(item.getProductId());
                    back.setQuantity(item.getQuantity());
                    back.setUnitPrice(item.getUnitPrice());
                    back.setCustomerId(returnObj.getCustomerId());
                    back.setOrderId(returnObj.getOrderId());
                    Long warehouseId = stockService.returnToStock(back, operatorId);

                    if (Integer.valueOf(2).equals(returnObj.getType())) {
                        // 换货：旧品收回后同等数量再发出，库存净额不变，但两条流水都留痕
                        back.setWarehouseId(warehouseId);
                        stockService.stockOut(back, operatorId);
                    }
                }
                // 销售退货冲减该客户的未结应收，不足部分保留在应收台账上
                BigDecimal returned = BigDecimal.ZERO;
                for (ReturnItem item : items) {
                    if (item.getUnitPrice() != null && item.getQuantity() != null) {
                        returned = returned.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
                reduceReceivable(returnObj.getCustomerId(), returnObj.getOrderId(), returned);
            }
        }

        returnObj.setStatus(status);
        returnMapper.updateById(returnObj);
    }

    /** 按退货金额冲减应收账款余额；只冲对应订单或该客户未结清的应收。 */
    private void reduceReceivable(Long customerId, Long orderId, BigDecimal amount) {
        if (customerId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LambdaQueryWrapper<AccountReceivable> w = new LambdaQueryWrapper<>();
        w.eq(AccountReceivable::getCustomerId, customerId)
         .ne(AccountReceivable::getStatus, 2)
         .orderByAsc(AccountReceivable::getCreateTime);
        if (orderId != null) {
            w.eq(AccountReceivable::getOrderId, orderId);
        }
        BigDecimal remain = amount;
        for (AccountReceivable ar : receivableMapper.selectList(w)) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal cut = ar.getAmount().subtract(ar.getPaidAmount()).min(remain);
            if (cut.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ar.setAmount(ar.getAmount().subtract(cut));
            remain = remain.subtract(cut);
            if (ar.getPaidAmount().compareTo(ar.getAmount()) >= 0) {
                ar.setStatus(2);
            } else if (ar.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                ar.setStatus(1);
            }
            receivableMapper.updateById(ar);
            syncOrderPaymentStatus(ar.getOrderId(), ar.getPaidAmount(), ar.getAmount());
        }
    }

    /** 按退货金额冲减应付账款余额；不足部分不强行摊派，保留在未冲销的退货单上。 */
    private void reducePayable(Long supplierId, BigDecimal amount) {
        if (supplierId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LambdaQueryWrapper<AccountPayable> w = new LambdaQueryWrapper<>();
        w.eq(AccountPayable::getSupplierId, supplierId)
         .ne(AccountPayable::getStatus, 2)
         .orderByAsc(AccountPayable::getCreateTime);
        BigDecimal remain = amount;
        for (AccountPayable ap : payableMapper.selectList(w)) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal cut = ap.getAmount().subtract(ap.getPaidAmount()).min(remain);
            if (cut.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ap.setAmount(ap.getAmount().subtract(cut));
            remain = remain.subtract(cut);
            if (ap.getPaidAmount().compareTo(ap.getAmount()) >= 0) {
                ap.setStatus(2);
            } else if (ap.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                ap.setStatus(1);
            }
            payableMapper.updateById(ap);
            syncPurchasePaymentStatus(ap.getPurchaseOrderId(), ap.getPaidAmount(), ap.getAmount());
        }
    }

    /** 应收变化后重算销售订单的收款状态，保持单据与台账一致。 */
    private void syncOrderPaymentStatus(Long orderId, BigDecimal paid, BigDecimal amount) {
        if (orderId == null || paid == null || amount == null) {
            return;
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        order.setPaymentStatus(paid.compareTo(amount) >= 0 ? 2 : (paid.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0));
        orderMapper.updateById(order);
    }

    /** 应付变化后重算采购订单的付款状态。 */
    private void syncPurchasePaymentStatus(Long purchaseOrderId, BigDecimal paid, BigDecimal amount) {
        if (purchaseOrderId == null || paid == null || amount == null) {
            return;
        }
        PurchaseOrder order = purchaseOrderMapper.selectById(purchaseOrderId);
        if (order == null) {
            return;
        }
        order.setPaymentStatus(paid.compareTo(amount) >= 0 ? 2 : (paid.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0));
        purchaseOrderMapper.updateById(order);
    }

    @Override
    public Page<Return> listPage(int pageNum, int pageSize, Integer status) {
        Page<Return> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Return> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Return::getStatus, status);
        }
        wrapper.orderByDesc(Return::getCreateTime);
        return returnMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ReturnItem> getReturnItems(Long returnId) {
        LambdaQueryWrapper<ReturnItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnItem::getReturnId, returnId);
        return returnItemMapper.selectList(wrapper);
    }
}