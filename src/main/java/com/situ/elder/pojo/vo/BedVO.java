package com.situ.elder.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BedVO {
    /**
     * 床位ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 床位编号
     */
    private String bedNo;

    /**
     * 房间号（入住分配联表查询用）
     */
    private String roomNo;

    /**
     * 楼栋ID（入住分配联表查询用）
     */
    private Long buildingId;

    /**
     * 楼栋名称（入住分配联表查询用）
     */
    private String buildingName;

    /**
     * 状态（0：空闲，1：入住，2：维修，3：停用）
     */
    private Integer status;

    /**
     * 床位费用/月
     */
    private BigDecimal price;

    /**
     * 入住老人姓名（空闲时为null）
     */
    private String elderName;

    /**
     * 入住时间（空闲时为null）
     */
    private Date checkInTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}
