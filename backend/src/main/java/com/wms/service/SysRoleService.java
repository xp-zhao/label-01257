package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.SysPermission;
import com.wms.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author WMS
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询
     */
    Page<SysRole> queryPage(Page<SysRole> page, String roleName, Integer status);

    /**
     * 获取所有角色
     */
    List<SysRole> getAllRoles();

    /**
     * 新增角色
     */
    void addRole(SysRole role);

    /**
     * 更新角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 获取权限树（用于角色权限分配）
     */
    List<SysPermission> getPermissionTree();
}
