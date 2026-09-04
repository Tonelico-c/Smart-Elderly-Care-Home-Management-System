package com.situ.elder.pojo.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AppElderLeaveDTO {
    /**
     * 请假原因
     */
    private String reason;

    /**
     * 请假地点
     */
    private String destination;

    /**
     * 联系电话
     */
    private String phone;


    /**
     * 预计外出时间（yyyy-MM-dd HH:mm）
     */
    private Date beginTime;

    /**
     * 预计返回时间（yyyy-MM-dd HH:mm）
     */
    private Date endTime;
}
