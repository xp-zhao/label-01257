package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Inventory;
import com.wms.entity.InventoryAdjust;
import com.wms.mapper.InventoryAdjustMapper;
import com.wms.mapper.InventoryMapper;
import com.wms.service.InventoryService;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final InventoryAdjustMapper adjustMapper;

    @Override
    public Page<Inventory> queryPage(Page<Inventory> page, Long warehouseId, Long productId, String batchNo) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                .eq(productId != null, Inventory::getProductId, productId)
                .like(StringUtils.hasText(batchNo), Inventory::getBatchNo, batchNo)
                .orderByDesc(Inventory::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Inventory> getWarningList() {
        return baseMapper.selectWarningList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(InventoryAdjust adjust) {
        Inventory inventory = this.getById(adjust.getInventoryId());
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        // 记录调整前数量
        adjust.setBeforeQuantity(inventory.getQuantity());
        adjust.setAfterQuantity(inventory.getQuantity() + adjust.getAdjustQuantity());
        adjust.setAdjustNo("ADJ" + IdUtil.getSnowflakeNextIdStr());
        adjust.setProductId(inventory.getProductId());
        adjust.setProductCode(inventory.getProductCode());
        adjust.setProductName(inventory.getProductName());
        adjust.setWarehouseId(inventory.getWarehouseId());

        // 更新库存
        inventory.setQuantity(adjust.getAfterQuantity());
        inventory.setAvailableQty(inventory.getAvailableQty() + adjust.getAdjustQuantity());
        this.updateById(inventory);

        // 保存调整记录
        adjustMapper.insert(adjust);
        log.info("库存调整成功: {} {} {}", adjust.getProductName(), adjust.getAdjustType(), adjust.getAdjustQuantity());
    }

    @Override
    public Page<InventoryAdjust> queryAdjustPage(Page<InventoryAdjust> page, Long productId, String adjustType) {
        LambdaQueryWrapper<InventoryAdjust> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, InventoryAdjust::getProductId, productId)
                .eq(StringUtils.hasText(adjustType), InventoryAdjust::getAdjustType, adjustType)
                .orderByDesc(InventoryAdjust::getCreateTime);
        return adjustMapper.selectPage(page, wrapper);
    }
}
