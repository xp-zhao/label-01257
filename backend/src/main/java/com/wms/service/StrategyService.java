package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.PickingStrategy;
import com.wms.entity.ReplenishStrategy;
import com.wms.entity.StorageStrategy;

/**
 * 策略服务接口
 *
 * @author WMS
 */
public interface StrategyService {

    // 存储策略
    Page<StorageStrategy> queryStoragePage(Page<StorageStrategy> page, String strategyName, Long warehouseId);
    void addStorageStrategy(StorageStrategy strategy);
    void updateStorageStrategy(StorageStrategy strategy);
    void deleteStorageStrategy(Long id);

    // 拣货策略
    Page<PickingStrategy> queryPickingPage(Page<PickingStrategy> page, String strategyName, Long warehouseId);
    void addPickingStrategy(PickingStrategy strategy);
    void updatePickingStrategy(PickingStrategy strategy);
    void deletePickingStrategy(Long id);

    // 补货策略
    Page<ReplenishStrategy> queryReplenishPage(Page<ReplenishStrategy> page, String strategyName, Long warehouseId);
    void addReplenishStrategy(ReplenishStrategy strategy);
    void updateReplenishStrategy(ReplenishStrategy strategy);
    void deleteReplenishStrategy(Long id);
}
