package com.wms.controller;

import com.wms.common.Result;
import com.wms.dto.LoginDTO;
import com.wms.service.AuthService;
import com.wms.vo.LoginVO;
import com.wms.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
