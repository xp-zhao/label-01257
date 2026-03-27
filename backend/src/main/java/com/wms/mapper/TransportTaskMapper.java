package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.TransportTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运输任务Mapper
 *
 * @author WMS
 */
@Mapper
public interface TransportTaskMapper extends BaseMapper<TransportTask> {
}
