package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.InboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单明细Mapper
 *
 * @author WMS
 */
@Mapper
public interface InboundOrderItemMapper extends BaseMapper<InboundOrderItem> {
}
