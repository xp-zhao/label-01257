package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Inventory;
import com.wms.entity.InventoryAdjust;

import java.util.List;

/**
 * 库存服务接口
 *
 * @author WMS
 */
public interface InventoryService extends IService<Inventory> {

    Page<Inventory> queryPage(Page<Inventory> page, Long warehouseId, Long productId, String batchNo);

    List<Inventory> getWarningList();

    void adjustInventory(InventoryAdjust adjust);

    Page<InventoryAdjust> queryAdjustPage(Page<InventoryAdjust> page, Long productId, String adjustType);
}
