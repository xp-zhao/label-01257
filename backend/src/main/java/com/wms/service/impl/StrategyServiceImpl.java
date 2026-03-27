package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StorageStrategyMapper storageMapper;
    private final PickingStrategyMapper pickingMapper;
    private final ReplenishStrategyMapper replenishMapper;
    private final WarehouseMapper warehouseMapper;
    private final ProductMapper productMapper;

    // ========== 存储策略 ==========
    @Override
    public Page<StorageStrategy> queryStoragePage(Page<StorageStrategy> page, String strategyName, Long warehouseId) {
        LambdaQueryWrapper<StorageStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(strategyName), StorageStrategy::getStrategyName, strategyName)
                .eq(warehouseId != null, StorageStrategy::getWarehouseId, warehouseId)
                .orderByDesc(StorageStrategy::getPriority);
        Page<StorageStrategy> result = storageMapper.selectPage(page, wrapper);
        result.getRecords().forEach(s -> {
            if (s.getWarehouseId() != null) {
                Warehouse w = warehouseMapper.selectById(s.getWarehouseId());
                if (w != null) s.setWarehouseName(w.getWarehouseName());
            }
        });
        return result;
    }

    @Override
    public void addStorageStrategy(StorageStrategy strategy) {
        storageMapper.insert(strategy);
        log.info("新增存储策略: {}", strategy.getStrategyName());
    }

    @Override
    public void updateStorageStrategy(StorageStrategy strategy) {
        storageMapper.updateById(strategy);
        log.info("更新存储策略: {}", strategy.getStrategyName());
    }

    @Override
    public void deleteStorageStrategy(Long id) {
        StorageStrategy s = storageMapper.selectById(id);
        if (s == null) throw new BusinessException("策略不存在");
        storageMapper.deleteById(id);
        log.info("删除存储策略: {}", s.getStrategyName());
    }

    // ========== 拣货策略 ==========
    @Override
    public Page<PickingStrategy> queryPickingPage(Page<PickingStrategy> page, String strategyName, Long warehouseId) {
        LambdaQueryWrapper<PickingStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(strategyName), PickingStrategy::getStrategyName, strategyName)
                .eq(warehouseId != null, PickingStrategy::getWarehouseId, warehouseId)
                .orderByDesc(PickingStrategy::getPriority);
        Page<PickingStrategy> result = pickingMapper.selectPage(page, wrapper);
        result.getRecords().forEach(s -> {
            if (s.getWarehouseId() != null) {
                Warehouse w = warehouseMapper.selectById(s.getWarehouseId());
                if (w != null) s.setWarehouseName(w.getWarehouseName());
            }
        });
        return result;
    }

    @Override
    public void addPickingStrategy(PickingStrategy strategy) {
        pickingMapper.insert(strategy);
        log.info("新增拣货策略: {}", strategy.getStrategyName());
    }

    @Override
    public void updatePickingStrategy(PickingStrategy strategy) {
        pickingMapper.updateById(strategy);
        log.info("更新拣货策略: {}", strategy.getStrategyName());
    }

    @Override
    public void deletePickingStrategy(Long id) {
        PickingStrategy s = pickingMapper.selectById(id);
        if (s == null) throw new BusinessException("策略不存在");
        pickingMapper.deleteById(id);
        log.info("删除拣货策略: {}", s.getStrategyName());
    }

    // ========== 补货策略 ==========
    @Override
    public Page<ReplenishStrategy> queryReplenishPage(Page<ReplenishStrategy> page, String strategyName, Long warehouseId) {
        LambdaQueryWrapper<ReplenishStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(strategyName), ReplenishStrategy::getStrategyName, strategyName)
                .eq(warehouseId != null, ReplenishStrategy::getWarehouseId, warehouseId)
                .orderByDesc(ReplenishStrategy::getCreateTime);
        Page<ReplenishStrategy> result = replenishMapper.selectPage(page, wrapper);
        result.getRecords().forEach(s -> {
            if (s.getWarehouseId() != null) {
                Warehouse w = warehouseMapper.selectById(s.getWarehouseId());
                if (w != null) s.setWarehouseName(w.getWarehouseName());
            }
            if (s.getProductId() != null) {
                Product p = productMapper.selectById(s.getProductId());
                if (p != null) s.setProductName(p.getProductName());
            }
        });
        return result;
    }

    @Override
    public void addReplenishStrategy(ReplenishStrategy strategy) {
        // 验证最大库存必须大于最小库存
        if (strategy.getMaxQuantity() != null && strategy.getMinQuantity() != null
                && strategy.getMaxQuantity() <= strategy.getMinQuantity()) {
            throw new BusinessException("最大库存必须大于最小库存");
        }
        replenishMapper.insert(strategy);
        log.info("新增补货策略: {}", strategy.getStrategyName());
    }

    @Override
    public void updateReplenishStrategy(ReplenishStrategy strategy) {
        // 验证最大库存必须大于最小库存
        if (strategy.getMaxQuantity() != null && strategy.getMinQuantity() != null
                && strategy.getMaxQuantity() <= strategy.getMinQuantity()) {
            throw new BusinessException("最大库存必须大于最小库存");
        }
        replenishMapper.updateById(strategy);
        log.info("更新补货策略: {}", strategy.getStrategyName());
    }

    @Override
    public void deleteReplenishStrategy(Long id) {
        ReplenishStrategy s = replenishMapper.selectById(id);
        if (s == null) throw new BusinessException("策略不存在");
        replenishMapper.deleteById(id);
        log.info("删除补货策略: {}", s.getStrategyName());
    }
}
