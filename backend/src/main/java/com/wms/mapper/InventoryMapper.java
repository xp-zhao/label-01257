package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 库存Mapper
 *
 * @author WMS
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 查询库存预警列表
     */
    List<Inventory> selectWarningList();

    /**
     * 库存统计
     */
    List<Map<String, Object>> selectInventoryStats(@Param("warehouseId") Long warehouseId);
}
