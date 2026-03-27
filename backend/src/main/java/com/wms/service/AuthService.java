package com.wms.service;

import com.wms.dto.LoginDTO;
import com.wms.vo.LoginVO;
import com.wms.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author WMS
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 登出
     */
    void logout();
}
