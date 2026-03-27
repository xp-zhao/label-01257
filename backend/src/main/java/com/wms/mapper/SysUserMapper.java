package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author WMS
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
