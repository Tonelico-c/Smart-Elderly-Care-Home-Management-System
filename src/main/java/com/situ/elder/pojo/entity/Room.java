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
 * 
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Room implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 楼栋ID
     */
    @TableField("building_id")
    private Long buildingId;

    /**
     * 房间号
     */
    @TableField("room_no")
    private String roomNo;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 		1 单人间		2 双人间		3 多人间		
     */
    @TableField("room_type")
    private Integer roomType;

    /**
     * 床位数量
     */
    @TableField("bed_count")
    private Integer bedCount;

    /**
     * 		0 空闲		1 部分入住		2 已满		3 维修		
     */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
