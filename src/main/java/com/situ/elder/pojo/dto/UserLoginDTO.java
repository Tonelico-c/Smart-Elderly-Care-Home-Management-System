package com.situ.elder.pojo.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String name;
    private String password;
    // 验证码
    private String captcha;
}
