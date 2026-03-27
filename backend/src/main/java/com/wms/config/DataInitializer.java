package com.wms.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.SysUser;
import com.wms.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据初始化器
 * 在应用启动时确保用户密码正确加密
 *
 * @author WMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查并更新默认用户的密码
        updateDefaultUserPasswords();
    }

    /**
     * 更新默认用户的密码
     * 确保密码使用正确的BCrypt编码
     */
    private void updateDefaultUserPasswords() {
        String defaultPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        List<SysUser> users = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getUsername, "admin", "warehouse", "operator")
        );
        
        for (SysUser user : users) {
            // 检查密码是否能正确验证
            if (!passwordEncoder.matches(defaultPassword, user.getPassword())) {
                log.info("更新用户 {} 的密码", user.getUsername());
                user.setPassword(encodedPassword);
                userMapper.updateById(user);
            }
        }
        
        log.info("默认用户密码检查完成");
    }
}
