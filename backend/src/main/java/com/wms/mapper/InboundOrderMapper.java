package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.InboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单Mapper
 *
 * @author WMS
 */
@Mapper
public interface InboundOrderMapper extends BaseMapper<InboundOrder> {
}
