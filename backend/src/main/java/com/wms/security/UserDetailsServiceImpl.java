package com.wms.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.SysUser;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 *
 * @author WMS
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 查询用户权限
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
