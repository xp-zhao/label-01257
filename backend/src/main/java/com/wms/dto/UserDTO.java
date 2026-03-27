package com.wms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户DTO
 *
 * @author WMS
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private Integer status;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
