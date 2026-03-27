package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.InventoryAdjust;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存调整记录Mapper
 *
 * @author WMS
 */
@Mapper
public interface InventoryAdjustMapper extends BaseMapper<InventoryAdjust> {
}
