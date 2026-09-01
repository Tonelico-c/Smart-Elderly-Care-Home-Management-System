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
public class Building implements Serializable {


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
     * 0停用 1启用
     */
    private Integer status;

    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
