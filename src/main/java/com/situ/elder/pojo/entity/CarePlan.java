package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 护理计划表
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CarePlan implements Serializable {


    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 老人ID
     */
    @TableField("elder_id")
    private Long elderId;

    /**
     * 护理人员ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 护理等级ID
     */
    @TableField("care_level_id")
    private Long careLevelId;

    /**
     * 计划名称
     */
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("start_date")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("end_date")
    private Date endDate;

    /**
     * 状态 0结束 1开始
     */
    private Integer status;

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
     * 护理计划明细（非数据库字段）
     */
    @TableField(exist = false)
    private List<CarePlanItem> items;


}
