package com.situ.elder.pojo.dto;

import lombok.Data;

/**
 * 老人端修改基本资料入参
 */
@Data
public class ElderInfoUpdateDTO {
    /**
     * 老人姓名
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 家庭住址
     */
    private String address;
}
