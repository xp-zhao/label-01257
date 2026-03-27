package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author WMS
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
