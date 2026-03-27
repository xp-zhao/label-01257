package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.OutboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单明细Mapper
 *
 * @author WMS
 */
@Mapper
public interface OutboundOrderItemMapper extends BaseMapper<OutboundOrderItem> {
}
