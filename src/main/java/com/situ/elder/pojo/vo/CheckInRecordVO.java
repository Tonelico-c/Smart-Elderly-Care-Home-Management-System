package com.situ.elder.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * 入住记录 VO（联表查询结果）
 */
@Data
public class CheckInRecordVO {

    private Long id;

    private Long elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 联系电话
     */
    private String phone;

    private Long buildingId;

    /**
     * 楼栋名称
     */
    private String buildingName;

    private Long roomId;

    /**
     * 房间号
     */
    private String roomNo;

    private Long bedId;

    /**
     * 床位号
     */
    private String bedNo;


    /**
     * 入住时间
     */
    private Date checkInTime;

    /**
     * 退住时间
     */
    private Date checkOutTime;

    /**
     * 状态（1：入住中，0：已退住）
     */
    private Integer status;

    private Date createTime;
}
