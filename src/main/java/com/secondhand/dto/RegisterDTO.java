package com.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册入参。只暴露允许客户端设置的字段，role / status 一律由服务端赋值，避免注册即提权。
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需在 4~20 位之间")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6~20 位之间")
    private String password;

    @Size(max = 50, message = "昵称最长 50 个字符")
    private String nickname;

    @Size(max = 20, message = "手机号最长 20 个字符")
    private String phone;
}
