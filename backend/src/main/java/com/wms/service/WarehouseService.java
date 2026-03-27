package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Warehouse;

import java.util.List;
import java.util.Map;

/**
 * 库房服务接口
 *
 * @author WMS
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询
     */
    Page<Warehouse> queryPage(Page<Warehouse> page, String warehouseCode, String warehouseName, Integer status);

    /**
     * 获取所有库房
     */
    List<Warehouse> getAllWarehouses();

    /**
     * 新增库房
     */
    void addWarehouse(Warehouse warehouse);

    /**
     * 更新库房
     */
    void updateWarehouse(Warehouse warehouse);

    /**
     * 删除库房
     */
    void deleteWarehouse(Long id);

    /**
     * 获取库房统计信息
     */
    Map<String, Object> getWarehouseStats(Long id);

    /**
     * 获取库房剩余可用面积
     * @param warehouseId 库房ID
     * @param excludeAreaId 排除的区域ID（编辑时排除自身）
     */
    Map<String, Object> getRemainingArea(Long warehouseId, Long excludeAreaId);
}
