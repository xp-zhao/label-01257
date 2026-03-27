package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.SysPermission;
import com.wms.entity.SysRole;
import com.wms.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService roleService;

    @GetMapping
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        Page<SysRole> page = roleService.queryPage(new Page<>(pageNum, pageSize), roleName, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> getAll() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysRole role) {
        roleService.addRole(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SysRole role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取权限树（用于角色权限分配）
     */
    @GetMapping("/permissions/tree")
    public Result<List<SysPermission>> getPermissionTree() {
        return Result.success(roleService.getPermissionTree());
    }
}
