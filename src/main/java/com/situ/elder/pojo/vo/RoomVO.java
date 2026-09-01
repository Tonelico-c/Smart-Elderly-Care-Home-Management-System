package com.situ.elder.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class RoomVO {
    /**
     * 房间ID
     */
    private Long id;

    /**
     * 楼栋ID
     */
    @TableField("building_id")
    private Long buildingId;

    /**
     * 楼栋名称
     */
    private String buildingName;

    /**
     * 房间号
     */
    private String roomNo;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 房间类型（1：单人间，2：双人间，3：多人间）
     */
    private Integer roomType;

    /**
     * 床位数量
     */
    private Integer bedCount;

    /**
     * 状态（0：空闲，1：部分入住，2：已满，3：维修）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    // 已入住人数
    private Integer residentCount;

}
