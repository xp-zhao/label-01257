package com.wms.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.InboundService;
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
public class InboundServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundService {

    private final InboundOrderItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<InboundOrder> queryPage(Page<InboundOrder> page, String orderNo, Long warehouseId, Integer status) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), InboundOrder::getOrderNo, orderNo)
                .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
                .eq(status != null, InboundOrder::getStatus, status)
                .orderByDesc(InboundOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public InboundOrder getDetail(Long id) {
        InboundOrder order = this.getById(id);
        if (order != null) {
            List<InboundOrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<InboundOrderItem>().eq(InboundOrderItem::getOrderId, id));
            order.setItems(items);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(InboundOrder order) {
        // 生成单号
        String orderNo = "IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + IdUtil.getSnowflakeNextIdStr().substring(14);
        order.setOrderNo(orderNo);
        order.setStatus(0); // 草稿

        // 设置库房名称
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) {
            order.setWarehouseName(warehouse.getWarehouseName());
        }

        // 计算总数量
        int totalQty = 0;
        if (order.getItems() != null) {
            for (InboundOrderItem item : order.getItems()) {
                totalQty += item.getPlanQuantity();
            }
        }
        order.setTotalQuantity(totalQty);
        order.setActualQuantity(0);

        this.save(order);

        // 保存明细
        if (order.getItems() != null) {
            for (InboundOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    item.setProductCode(product.getProductCode());
                    item.setProductName(product.getProductName());
                }
                itemMapper.insert(item);
            }
        }
        log.info("创建入库单成功: {}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(InboundOrder order) {
        InboundOrder existing = this.getById(order.getId());
        if (existing == null) {
            throw new BusinessException("入库单不存在");
        }
        if (existing.getStatus() != 0) {
            throw new BusinessException("只能修改草稿状态的入库单");
        }

        // 删除原有明细
        itemMapper.delete(new LambdaQueryWrapper<InboundOrderItem>()
                .eq(InboundOrderItem::getOrderId, order.getId()));

        // 计算总数量
        int totalQty = 0;
        if (order.getItems() != null) {
            for (InboundOrderItem item : order.getItems()) {
                totalQty += item.getPlanQuantity();
            }
        }
        order.setTotalQuantity(totalQty);

        this.updateById(order);

        // 保存新明细
        if (order.getItems() != null) {
            for (InboundOrderItem item : order.getItems()) {
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
        log.info("更新入库单成功: {}", order.getOrderNo());
    }

    @Override
    public void submitOrder(Long id) {
        InboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能提交草稿状态的入库单");
        }
        order.setStatus(1); // 待入库
        this.updateById(order);
        log.info("提交入库单成功: {}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmInbound(Long id) {
        InboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("入库单状态不正确");
        }

        List<InboundOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<InboundOrderItem>().eq(InboundOrderItem::getOrderId, id));

        int totalActual = 0;
        for (InboundOrderItem item : items) {
            // 更新实际数量
            if (item.getActualQuantity() == null || item.getActualQuantity() == 0) {
                item.setActualQuantity(item.getPlanQuantity());
            }
            itemMapper.updateById(item);
            totalActual += item.getActualQuantity();

            // 更新库存
            Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getProductId, item.getProductId())
                    .eq(Inventory::getWarehouseId, order.getWarehouseId())
                    .eq(Inventory::getBatchNo, item.getBatchNo()));

            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + item.getActualQuantity());
                inventory.setAvailableQty(inventory.getAvailableQty() + item.getActualQuantity());
                inventoryMapper.updateById(inventory);
            } else {
                inventory = new Inventory();
                inventory.setProductId(item.getProductId());
                inventory.setProductCode(item.getProductCode());
                inventory.setProductName(item.getProductName());
                inventory.setWarehouseId(order.getWarehouseId());
                inventory.setWarehouseName(order.getWarehouseName());
                inventory.setBatchNo(item.getBatchNo());
                inventory.setQuantity(item.getActualQuantity());
                inventory.setAvailableQty(item.getActualQuantity());
                inventory.setLockedQty(0);
                inventory.setShelfId(item.getShelfId());
                inventory.setLocationCode(item.getLocationCode());
                inventory.setProductionDate(item.getProductionDate());
                inventory.setExpiryDate(item.getExpiryDate());
                inventoryMapper.insert(inventory);
            }
        }

        order.setActualQuantity(totalActual);
        order.setStatus(3); // 已完成
        order.setInboundTime(LocalDateTime.now());
        this.updateById(order);
        log.info("确认入库成功: {}", order.getOrderNo());
    }

    @Override
    public void cancelOrder(Long id) {
        InboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() == 3) {
            throw new BusinessException("已完成的入库单不能取消");
        }
        order.setStatus(4); // 已取消
        this.updateById(order);
        log.info("取消入库单成功: {}", order.getOrderNo());
    }
}
