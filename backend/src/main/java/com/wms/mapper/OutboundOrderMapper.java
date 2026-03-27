package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单Mapper
 *
 * @author WMS
 */
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {
}
