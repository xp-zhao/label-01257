package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.dto.UserDTO;
import com.wms.entity.SysUser;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysUserRoleMapper;
import com.wms.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> queryPage(Page<SysUser> page, String username, String realName, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO dto) {
        // 检查用户名是否存在
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(user);

        // 保存用户角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());

        log.info("新增用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO dto) {
        SysUser user = this.getById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否重复
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        user.setUsername(dto.getUsername());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        this.updateById(user);

        // 更新用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId()));
        saveUserRoles(user.getId(), dto.getRoleIds());

        log.info("更新用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账号");
        }
        this.removeById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        log.info("删除用户成功: {}", user.getUsername());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        this.updateById(user);
        log.info("更新用户状态成功: {} -> {}", user.getUsername(), status);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        this.updateById(user);
        log.info("重置用户密码成功: {}", user.getUsername());
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }
}
