package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.BusinessException;
import com.wms.dto.LoginDTO;
import com.wms.entity.SysPermission;
import com.wms.entity.SysRole;
import com.wms.entity.SysUser;
import com.wms.mapper.SysPermissionMapper;
import com.wms.mapper.SysRoleMapper;
import com.wms.mapper.SysUserMapper;
import com.wms.service.AuthService;
import com.wms.utils.JwtUtils;
import com.wms.vo.LoginVO;
import com.wms.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 获取角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 获取权限
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());

        // 构建返回对象
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(roleNames);
        vo.setPermissions(permissions);

        log.info("用户 {} 登录成功", dto.getUsername());
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 获取权限
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());

        // 获取菜单
        List<SysPermission> menus = permissionMapper.selectMenusByUserId(user.getId());
        List<SysPermission> menuTree = buildMenuTree(menus);

        // 构建返回对象
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(roleNames);
        vo.setPermissions(permissions);
        vo.setMenus(menuTree);

        return vo;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("用户登出成功");
    }

    /**
     * 构建菜单树
     */
    private List<SysPermission> buildMenuTree(List<SysPermission> menus) {
        List<SysPermission> tree = new ArrayList<>();
        for (SysPermission menu : menus) {
            // 顶级菜单：parentId 为 null 或 0
            if (menu.getParentId() == null || menu.getParentId().equals(0L)) {
                menu.setChildren(getChildren(menu.getId(), menus));
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 获取子菜单
     */
    private List<SysPermission> getChildren(Long parentId, List<SysPermission> menus) {
        List<SysPermission> children = new ArrayList<>();
        for (SysPermission menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                menu.setChildren(getChildren(menu.getId(), menus));
                children.add(menu);
            }
        }
        return children;
    }
}
