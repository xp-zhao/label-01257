package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author WMS
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询权限编码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysPermission> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
