package com.wms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改密码DTO
 *
 * @author WMS
 */
@Data
public class PasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
