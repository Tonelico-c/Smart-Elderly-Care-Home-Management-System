package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 护理任务与打卡记录表
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CareTask implements Serializable {


    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 老人ID
     */
    @TableField("elder_id")
    private Long elderId;

    /**
     * 来源护理计划ID
     */
    @TableField("care_plan_id")
    private Long carePlanId;

    /**
     * 护理项目ID
     */
    @TableField("care_item_id")
    private Long careItemId;

    /**
     * 护理项目名称(冗余，防止项目改名历史记录变动)
     */
    @TableField("care_item_name")
    private String careItemName;

    /**
     * 执行护理员ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 计划执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("plan_execute_date")
    private Date planExecuteDate;

    /**
     * 计划执行时间（HH:mm，对应数据库 time 类型）
     */
    @TableField("plan_execute_time")
    private String planExecuteTime;

    /**
     * 任务状态（0：待执行，1：已完成，2：已跳过/取消）
     */
    private Integer status;

    /**
     * 实际完成时间
     */
    @TableField("actual_execute_time")
    private Date actualExecuteTime;

    /**
     * 执行结果描述/健康数值
     */
    @TableField("execute_result")
    private String executeResult;

    /**
     * 现场打卡照片URL(多张以逗号隔开)
     */
    @TableField("execute_img")
    private String executeImg;

    /**
     * 护理员执行备注
     */
    private String remark;

    /**
     * 任务生成时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
