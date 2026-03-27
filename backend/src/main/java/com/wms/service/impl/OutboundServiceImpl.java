package com.wms.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.OutboundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundService {

    private final OutboundOrderItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<OutboundOrder> queryPage(Page<OutboundOrder> page, String orderNo, Long warehouseId, Integer status) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), OutboundOrder::getOrderNo, orderNo)
                .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
                .eq(status != null, OutboundOrder::getStatus, status)
                .orderByDesc(OutboundOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public OutboundOrder getDetail(Long id) {
        OutboundOrder order = this.getById(id);
        if (order != null) {
            List<OutboundOrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOrderId, id));
            order.setItems(items);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OutboundOrder order) {
        String orderNo = "OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.getSnowflakeNextIdStr().substring(14);
        order.setOrderNo(orderNo);
        order.setStatus(0);

        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) {
            order.setWarehouseName(warehouse.getWarehouseName());
        }

        int totalQty = 0;
        if (order.getItems() != null) {
            for (OutboundOrderItem item : order.getItems()) {
                totalQty += item.getPlanQuantity();
            }
        }
        order.setTotalQuantity(totalQty);
        order.setActualQuantity(0);

        this.save(order);

        if (order.getItems() != null) {
            for (OutboundOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductCode(product.getProductCode());
                    item.setProductName(product.getProductName());
                }
                itemMapper.insert(item);
            }
        }
        log.info("创建出库单成功: {}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(OutboundOrder order) {
        OutboundOrder existing = this.getById(order.getId());
        if (existing == null) {
            throw new BusinessException("出库单不存在");
        }
        if (existing.getStatus() != 0) {
            throw new BusinessException("只能修改草稿状态的出库单");
        }

        itemMapper.delete(new LambdaQueryWrapper<OutboundOrderItem>()
                .eq(OutboundOrderItem::getOrderId, order.getId()));

        int totalQty = 0;
        if (order.getItems() != null) {
            for (OutboundOrderItem item : order.getItems()) {
                totalQty += item.getPlanQuantity();
            }
        }
        order.setTotalQuantity(totalQty);

        this.updateById(order);

        if (order.getItems() != null) {
            for (OutboundOrderItem item : order.getItems()) {
                item.setId(null);
                item.setOrderId(order.getId());
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductCode(product.getProductCode());
                    item.setProductName(product.getProductName());
                }
                itemMapper.insert(item);
            }
        }
        log.info("更新出库单成功: {}", order.getOrderNo());
    }

    @Override
    public void submitOrder(Long id) {
        OutboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能提交草稿状态的出库单");
        }
        order.setStatus(1);
        this.updateById(order);
        log.info("提交出库单成功: {}", order.getOrderNo());
    }

    @Override
    public void auditOrder(Long id, boolean approved, String auditorName) {
        OutboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只能审核待审核状态的出库单");
        }
        order.setStatus(approved ? 2 : 5);
        order.setAuditorName(auditorName);
        order.setAuditTime(LocalDateTime.now());
        this.updateById(order);
        log.info("审核出库单: {} -> {}", order.getOrderNo(), approved ? "通过" : "拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOutbound(Long id) {
        OutboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException("出库单状态不正确");
        }

        List<OutboundOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOrderId, id));

        int totalActual = 0;
        for (OutboundOrderItem item : items) {
            if (item.getActualQuantity() == null || item.getActualQuantity() == 0) {
                item.setActualQuantity(item.getPlanQuantity());
            }
            itemMapper.updateById(item);
            totalActual += item.getActualQuantity();

            // 扣减库存
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getProductId, item.getProductId())
                    .eq(Inventory::getWarehouseId, order.getWarehouseId())
                    .eq(item.getBatchNo() != null, Inventory::getBatchNo, item.getBatchNo())
                    .gt(Inventory::getAvailableQty, 0)
                    .orderByAsc(Inventory::getCreateTime)
                    .last("LIMIT 1"));

            if (inventory == null) {
                throw new BusinessException("商品 " + item.getProductName() + " 库存不足");
            }

            if (inventory.getAvailableQty() < item.getActualQuantity()) {
                throw new BusinessException("商品 " + item.getProductName() + " 库存不足");
            }

            inventory.setQuantity(inventory.getQuantity() - item.getActualQuantity());
            inventory.setAvailableQty(inventory.getAvailableQty() - item.getActualQuantity());
            inventoryMapper.updateById(inventory);
        }

        order.setActualQuantity(totalActual);
        order.setStatus(4);
        order.setOutboundTime(LocalDateTime.now());
        this.updateById(order);
        log.info("确认出库成功: {}", order.getOrderNo());
    }

    @Override
    public void cancelOrder(Long id) {
        OutboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() == 4) {
            throw new BusinessException("已完成的出库单不能取消");
        }
        order.setStatus(5);
        this.updateById(order);
        log.info("取消出库单成功: {}", order.getOrderNo());
    }
}
