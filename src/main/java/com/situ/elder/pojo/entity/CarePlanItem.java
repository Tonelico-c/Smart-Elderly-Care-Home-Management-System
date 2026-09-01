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
 * 护理计划明细表
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CarePlanItem implements Serializable {


    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 护理计划ID
     */
    @TableField("care_plan_id")
    private Long carePlanId;

    /**
     * 护理项目ID
     */
    @TableField("care_item_id")
    private Long careItemId;

    /**
     * 计划执行时间（HH:mm，对应数据库 time 类型）
     */
    @TableField("execute_time")
    private String executeTime;

    /**
     * 执行周期 0 天 1 周 2 月
     */
    @TableField("execute_cycle")
    private Integer executeCycle;

    /**
     * 执行频次
     */
    @TableField("execute_frequency")
    private Integer executeFrequency;

    /**
     * 备注
     */
    private String remark;

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


}
