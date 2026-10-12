package com.example.jxc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jxc.dto.StockOperateDTO;
import com.example.jxc.entity.Product;
import com.example.jxc.entity.ProductBatch;
import com.example.jxc.entity.ProductStock;
import com.example.jxc.entity.StockIn;
import com.example.jxc.entity.StockOut;
import com.example.jxc.exception.BusinessException;
import com.example.jxc.mapper.ProductBatchMapper;
import com.example.jxc.mapper.ProductMapper;
import com.example.jxc.mapper.ProductStockMapper;
import com.example.jxc.mapper.StockInMapper;
import com.example.jxc.mapper.StockOutMapper;
import com.example.jxc.mapper.UserMapper;
import com.example.jxc.entity.User;
import com.example.jxc.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class StockServiceImpl extends ServiceImpl<StockInMapper, StockIn> implements StockService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private ProductBatchMapper productBatchMapper;

    @Autowired
    private UserMapper userMapper;

    /** 数据隔离：超级管理员不受仓库限制；其余角色绑定了仓库就只看本仓。 */
    @Override
    public Long resolveWarehouseId(Long operatorId) {
        if (operatorId == null) {
            return null;
        }
        User u = userMapper.selectById(operatorId);
        if (u == null || "super_admin".equals(u.getRole())) {
            return null;
        }
        return u.getWarehouseId();
    }

    /**
     * 定位本次变动归属的仓库：显式指定 &gt; 操作人所属仓库 &gt; 商品仅有的一个台账仓库。
     * 商品分布在多个仓库又无法判断时，required 为真则要求调用方指定，避免只改总账不改台账。
     */
    private Long locateWarehouse(StockOperateDTO dto, Long operatorId, boolean required) {
        if (dto.getWarehouseId() != null) {
            return dto.getWarehouseId();
        }
        Long own = resolveWarehouseId(operatorId);
        if (own != null) {
            return own;
        }
        LambdaQueryWrapper<ProductStock> psWrapper = new LambdaQueryWrapper<>();
        psWrapper.eq(ProductStock::getProductId, dto.getProductId());
        List<ProductStock> ledger = productStockMapper.selectList(psWrapper);
        if (ledger.size() == 1) {
            return ledger.get(0).getWarehouseId();
        }
        if (required && ledger.size() > 1) {
            throw new BusinessException("\u8be5\u5546\u54c1\u5b58\u653e\u5728\u591a\u4e2a\u4ed3\u5e93\uff0c\u8bf7\u6307\u5b9a\u51fa\u5165\u5e93\u4ed3\u5e93\u6216\u7ed9\u64cd\u4f5c\u4eba\u7ed1\u5b9a\u4ed3\u5e93");
        }
        return null;
    }

    /** 未显式指定仓库时，非超管操作人落到自己的仓库。 */
    private Long effectiveWarehouse(StockOperateDTO dto, Long operatorId) {
        return locateWarehouse(dto, operatorId, true);
    }

    /**
     * 退货回补的归属仓库：优先按原出库流水回到出货仓，其次按通用规则定位；
     * 都定位不到时只调整总库存。
     */
    private Long warehouseForReturn(StockOperateDTO dto, Long operatorId) {
        if (dto.getWarehouseId() != null) {
            return dto.getWarehouseId();
        }
        LambdaQueryWrapper<StockOut> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(StockOut::getProductId, dto.getProductId())
                  .isNotNull(StockOut::getWarehouseId);
        if (dto.getOrderId() != null) {
            outWrapper.eq(StockOut::getOrderId, dto.getOrderId());
        }
        outWrapper.orderByDesc(StockOut::getId).last("LIMIT 1");
        StockOut lastOut = stockOutMapper.selectOne(outWrapper);
        if (lastOut != null && lastOut.getWarehouseId() != null) {
            return lastOut.getWarehouseId();
        }
        return locateWarehouse(dto, operatorId, false);
    }

    /** 乐观锁更新：影响 0 行说明库存已被其他事务改动，整笔回滚由调用方重试。 */
    private void saveProduct(Product product) {
        if (productMapper.updateById(product) == 0) {
            throw new BusinessException("\u5e93\u5b58\u5df2\u88ab\u5176\u4ed6\u64cd\u4f5c\u4fee\u6539\uff0c\u8bf7\u91cd\u8bd5");
        }
    }

    @Override
    @Transactional
    public void stockIn(StockOperateDTO dto, Long operatorId) {
        if (dto.getSupplierId() == null) {
            throw new BusinessException("\u4f9b\u5e94\u5546ID\u4e0d\u80fd\u4e3a\u7a7a");
        }

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        Long warehouseId = effectiveWarehouse(dto, operatorId);

        StockIn stockIn = new StockIn();
        stockIn.setProductId(dto.getProductId());
        stockIn.setSupplierId(dto.getSupplierId());
        stockIn.setQuantity(dto.getQuantity());
        stockIn.setUnitPrice(dto.getUnitPrice());
        stockIn.setOperatorId(operatorId);
        stockIn.setWarehouseId(warehouseId);
        stockIn.setBatchNo(dto.getBatchNo());
        stockInMapper.insert(stockIn);

        int oldStock = product.getStock();
        int addQty = dto.getQuantity();
        BigDecimal addCost = dto.getUnitPrice();

        if (product.getCostPrice() == null || oldStock == 0) {
            product.setCostPrice(addCost);
        } else {
            BigDecimal totalValue = product.getCostPrice().multiply(BigDecimal.valueOf(oldStock))
                .add(addCost.multiply(BigDecimal.valueOf(addQty)));
            product.setCostPrice(totalValue.divide(BigDecimal.valueOf(oldStock + addQty), 2, RoundingMode.HALF_UP));
        }

        product.setStock(oldStock + addQty);
        saveProduct(product);
        adjustWarehouseStock(dto.getProductId(), warehouseId, addQty);

        if (dto.getBatchNo() != null && !dto.getBatchNo().isEmpty()) {
            upsertBatch(dto, operatorId);
        }
    }

    /** 同一批次号入库时累加数量，避免一个批次号被登记成多行。 */
    private void upsertBatch(StockOperateDTO dto, Long operatorId) {
        LambdaQueryWrapper<ProductBatch> bw = new LambdaQueryWrapper<>();
        bw.eq(ProductBatch::getProductId, dto.getProductId())
          .eq(ProductBatch::getBatchNo, dto.getBatchNo());
        if (dto.getSerialNo() != null && !dto.getSerialNo().isEmpty()) {
            bw.eq(ProductBatch::getSerialNo, dto.getSerialNo());
        } else {
            bw.and(w -> w.isNull(ProductBatch::getSerialNo).or().eq(ProductBatch::getSerialNo, ""));
        }
        ProductBatch batch = productBatchMapper.selectOne(bw);
        if (batch == null) {
            batch = new ProductBatch();
            batch.setProductId(dto.getProductId());
            batch.setBatchNo(dto.getBatchNo());
            batch.setSerialNo(dto.getSerialNo());
            batch.setProductionDate(dto.getProductionDate());
            batch.setExpiryDate(dto.getExpiryDate());
            batch.setQuantity(dto.getQuantity());
            batch.setSupplierId(dto.getSupplierId());
            batch.setPurchasePrice(dto.getUnitPrice());
            batch.setStatus(1);
            productBatchMapper.insert(batch);
        } else {
            if (batch.getExpiryDate() == null && dto.getExpiryDate() != null) {
                batch.setExpiryDate(dto.getExpiryDate());
            }
            if (batch.getProductionDate() == null && dto.getProductionDate() != null) {
                batch.setProductionDate(dto.getProductionDate());
            }
            batch.setQuantity((batch.getQuantity() == null ? 0 : batch.getQuantity()) + dto.getQuantity());
            if (batch.getStatus() != null && batch.getStatus() == 2 && batch.getQuantity() > 0) {
                batch.setStatus(1);
            }
            productBatchMapper.updateById(batch);
        }
    }

    /**
     * 出库按“先到期先出”核销批次：无有效期的批次排在最后。
     * 批次只登记到商品维度（表里没有仓库列），因此按商品全部批次排序。
     * 历史入库未登记批次时允许核销不完，不阻断出库。
     */
    private void consumeBatches(Long productId, int quantity) {
        LambdaQueryWrapper<ProductBatch> bw = new LambdaQueryWrapper<>();
        bw.eq(ProductBatch::getProductId, productId)
          .eq(ProductBatch::getStatus, 1)
          .gt(ProductBatch::getQuantity, 0);
        List<ProductBatch> batches = productBatchMapper.selectList(bw);
        batches.sort(Comparator.comparing(
                (ProductBatch b) -> b.getExpiryDate() == null ? LocalDate.of(9999, 12, 31) : b.getExpiryDate())
            .thenComparing(ProductBatch::getId));

        int remain = quantity;
        for (ProductBatch b : batches) {
            if (remain <= 0) {
                break;
            }
            int have = b.getQuantity() == null ? 0 : b.getQuantity();
            int take = Math.min(have, remain);
            if (take <= 0) {
                continue;
            }
            b.setQuantity(have - take);
            if (b.getQuantity() == 0) {
                b.setStatus(2);
            }
            productBatchMapper.updateById(b);
            remain -= take;
        }
    }

    @Override
    @Transactional
    public Long returnToStock(StockOperateDTO dto, Long operatorId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        Long warehouseId = warehouseForReturn(dto, operatorId);

        StockIn stockIn = new StockIn();
        stockIn.setProductId(dto.getProductId());
        stockIn.setSupplierId(dto.getSupplierId());
        stockIn.setQuantity(dto.getQuantity());
        stockIn.setUnitPrice(dto.getUnitPrice());
        stockIn.setOperatorId(operatorId);
        stockIn.setWarehouseId(warehouseId);
        stockIn.setBatchNo(dto.getBatchNo());
        stockInMapper.insert(stockIn);

        // 退货回补按现行加权成本入账，不参与成本重算；成本只在采购入库时更新
        product.setStock(product.getStock() + dto.getQuantity());
        saveProduct(product);
        adjustWarehouseStock(dto.getProductId(), warehouseId, dto.getQuantity());
        // 退货明细没有批次维度：带批次号才回补该批次，否则只调整库存，批次数量可在批次管理页校正
        if (dto.getBatchNo() != null && !dto.getBatchNo().isEmpty()) {
            upsertBatch(dto, operatorId);
        }
        return warehouseId;
    }

    @Override
    @Transactional
    public void stockOut(StockOperateDTO dto, Long operatorId) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        if (product.getStock() < dto.getQuantity()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3\uff0c\u5f53\u524d\u5e93\u5b58: " + product.getStock());
        }

        // 可用库存 = 总库存 - 订单预留；否则手工出库会把已下单未发的货抢走，导致发货时无货可发
        int reserved = product.getReservedStock() == null ? 0 : product.getReservedStock();
        if (product.getStock() - reserved < dto.getQuantity()) {
            throw new BusinessException("\u53ef\u7528\u5e93\u5b58\u4e0d\u8db3\uff1a\u603b\u5e93\u5b58 " + product.getStock()
                + "\uff0c\u5df2\u88ab\u8ba2\u5355\u9884\u7559 " + reserved);
        }

        Long warehouseId = effectiveWarehouse(dto, operatorId);

        StockOut stockOut = new StockOut();
        stockOut.setProductId(dto.getProductId());
        stockOut.setCustomerId(dto.getCustomerId());
        stockOut.setOrderId(dto.getOrderId());
        stockOut.setQuantity(dto.getQuantity());
        stockOut.setUnitPrice(dto.getUnitPrice());
        stockOut.setCostPrice(product.getCostPrice());
        stockOut.setOperatorId(operatorId);
        stockOut.setWarehouseId(warehouseId);
        stockOutMapper.insert(stockOut);

        // 出库只按当前加权平均成本结转，不重算成本；加权平均成本仅在入库时更新
        product.setStock(product.getStock() - dto.getQuantity());
        saveProduct(product);
        adjustWarehouseStock(dto.getProductId(), warehouseId, -dto.getQuantity());
        consumeBatches(dto.getProductId(), dto.getQuantity());
    }

    @Override
    public void adjustWarehouseStock(Long productId, Long warehouseId, int delta) {
        if (productId == null || warehouseId == null) {
            return;
        }
        int quantityChange = delta;
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId)
               .eq(ProductStock::getWarehouseId, warehouseId);
        ProductStock ps = productStockMapper.selectOne(wrapper);

        if (ps == null) {
            ps = new ProductStock();
            ps.setProductId(productId);
            ps.setWarehouseId(warehouseId);
            ps.setQuantity(Math.max(0, quantityChange));
            productStockMapper.insert(ps);
        } else {
            int newQty = ps.getQuantity() + quantityChange;
            ps.setQuantity(Math.max(0, newQty));
            productStockMapper.updateById(ps);
        }
    }

    @Override
    public Page<StockIn> listStockIn(int pageNum, int pageSize, Long operatorId) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        Long scope = resolveWarehouseId(operatorId);
        if (scope != null) {
            wrapper.eq(StockIn::getWarehouseId, scope);
        }
        wrapper.orderByDesc(StockIn::getCreateTime);
        return stockInMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<StockOut> listStockOut(int pageNum, int pageSize, Long operatorId) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        Long scope = resolveWarehouseId(operatorId);
        if (scope != null) {
            wrapper.eq(StockOut::getWarehouseId, scope);
        }
        wrapper.orderByDesc(StockOut::getCreateTime);
        return stockOutMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void cancelStockIn(Long stockInId, Long operatorId) {
        StockIn stockIn = stockInMapper.selectById(stockInId);
        if (stockIn == null) {
            throw new BusinessException("\u5165\u5e93\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }

        Product product = productMapper.selectById(stockIn.getProductId());
        if (product == null) {
            throw new BusinessException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }

        if (product.getStock() < stockIn.getQuantity()) {
            throw new BusinessException("\u5e93\u5b58\u4e0d\u8db3\uff0c\u65e0\u6cd5\u64a4\u9500\u5165\u5e93");
        }

        int reservedNow = product.getReservedStock() == null ? 0 : product.getReservedStock();
        if (product.getStock() - reservedNow < stockIn.getQuantity()) {
            throw new BusinessException("\u8be5\u6279\u6b21\u5df2\u88ab\u8ba2\u5355\u9884\u7559 " + reservedNow
                + "\uff0c\u65e0\u6cd5\u64a4\u9500\u5165\u5e93");
        }

        Long scope = resolveWarehouseId(operatorId);
        if (scope != null && !scope.equals(stockIn.getWarehouseId())) {
            throw new BusinessException("\u53ea\u80fd\u64a4\u9500\u672c\u4ed3\u5e93\u7684\u5165\u5e93\u8bb0\u5f55");
        }

        product.setStock(product.getStock() - stockIn.getQuantity());
        saveProduct(product);

        if (stockIn.getWarehouseId() != null) {
            adjustWarehouseStock(stockIn.getProductId(), stockIn.getWarehouseId(), -stockIn.getQuantity());
        }

        stockInMapper.deleteById(stockInId);
    }
}