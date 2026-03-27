package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.WarehouseArea;

import java.util.List;

/**
 * 库房区域服务接口
 *
 * @author WMS
 */
public interface WarehouseAreaService extends IService<WarehouseArea> {

    Page<WarehouseArea> queryPage(Page<WarehouseArea> page, Long warehouseId, String areaCode, String areaType);

    List<WarehouseArea> getAreasByWarehouseId(Long warehouseId);

    void addArea(WarehouseArea area);

    void updateArea(WarehouseArea area);

    void deleteArea(Long id);
}
