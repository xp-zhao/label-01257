package com.wms.vo;

import com.wms.entity.SysPermission;
import lombok.Data;

import java.util.List;

/**
 * 用户信息VO
 *
 * @author WMS
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 菜单列表
     */
    private List<SysPermission> menus;
}
