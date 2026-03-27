package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.SysPermission;
import com.wms.entity.SysRole;
import com.wms.entity.SysRolePermission;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysPermissionMapper;
import com.wms.mapper.SysRoleMapper;
import com.wms.mapper.SysRolePermissionMapper;
import com.wms.mapper.SysUserRoleMapper;
import com.wms.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public Page<SysRole> queryPage(Page<SysRole> page, String roleName, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .eq(status != null, SysRole::getStatus, status)
                .orderByDesc(SysRole::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<SysRole> getAllRoles() {
        return this.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getId));
    }

    @Override
    public void addRole(SysRole role) {
        long count = this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, role.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        this.save(role);
        log.info("新增角色成功: {}", role.getRoleName());
    }

    @Override
    public void updateRole(SysRole role) {
        SysRole existing = this.getById(role.getId());
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        long count = this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, role.getRoleCode())
                .ne(SysRole::getId, role.getId()));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        this.updateById(role);
        log.info("更新角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // 检查是否有用户关联
        long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("该角色下有用户，不能删除");
        }
        this.removeById(id);
        // 删除角色权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, id));
        log.info("删除角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        // 保存新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
        log.info("分配角色权限成功: roleId={}", roleId);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return permissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<SysPermission> getPermissionTree() {
        // 获取所有权限
        List<SysPermission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSort)
        );
        // 构建树形结构
        return buildTree(allPermissions);
    }

    /**
     * 构建权限树
     */
    private List<SysPermission> buildTree(List<SysPermission> permissions) {
        // 按父ID分组
        Map<Long, List<SysPermission>> parentMap = permissions.stream()
                .collect(Collectors.groupingBy(SysPermission::getParentId));
        // 设置子节点
        permissions.forEach(p -> p.setChildren(parentMap.get(p.getId())));
        // 返回顶级节点（parentId = 0）
        return permissions.stream()
                .filter(p -> p.getParentId() == null || p.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
