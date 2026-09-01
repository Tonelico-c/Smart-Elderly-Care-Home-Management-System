package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 入住记录表
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CheckInRecord implements Serializable {


    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 老人ID
     */
    @TableField("elder_id")
    private Long elderId;

    /**
     * 楼栋ID
     */
    @TableField("building_id")
    private Long buildingId;

    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 床位ID
     */
    @TableField("bed_id")
    private Long bedId;

    /**
     * 入住护理等级
     */
    @TableField("care_level_id")
    private Long careLevelId;

    /**
     * 入住时间
     */
    @TableField("check_in_time")
    private Date checkInTime;

    /**
     * 退住时间
     */
    @TableField("check_out_time")
    private Date checkOutTime;

    /**
     * 状态（1：入住中，0：已退住）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;


}
