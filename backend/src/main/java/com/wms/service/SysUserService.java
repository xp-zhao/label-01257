package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.dto.UserDTO;
import com.wms.entity.SysUser;

/**
 * 用户服务接口
 *
 * @author WMS
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询
     */
    Page<SysUser> queryPage(Page<SysUser> page, String username, String realName, Integer status);

    /**
     * 新增用户
     */
    void addUser(UserDTO dto);

    /**
     * 更新用户
     */
    void updateUser(UserDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 更新状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long id);
}
