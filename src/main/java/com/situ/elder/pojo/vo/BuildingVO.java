package com.situ.elder.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class BuildingVO {
    /**
     * 楼栋ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 楼栋编号
     */
    @TableField("building_no")
    private String buildingNo;

    /**
     * 楼栋名称
     */
    @TableField("building_name")
    private String buildingName;

    /**
     * 楼层数量
     */
    @TableField("floor_count")
    private Integer floorCount;

    /**
     * 状态（0：停用，1：启用）
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 房间数
     */
    private Integer roomCount;
    /**
     * 床位数
     */
    private Integer bedCount;
    /**
     * 入住人数
     */
    private Integer residentCount;
    /**
     * 空闲床位数量
     */
    private Integer freeBedCount;

}
