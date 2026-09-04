package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 老人请假外出记录表
 * </p>
 *
 * @author Gao
 * @since 2026-09-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ElderLeave implements Serializable {


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
     * 请假事由
     */
    private String reason;

    /**
     * 外出去向
     */
    private String destination;

    /**
     * 外出期间联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 预计外出时间
     */
    @TableField("begin_time")
    private Date beginTime;

    /**
     * 预计返回时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 实际返回时间（销假时写入）
     */
    @TableField("actual_return_time")
    private Date actualReturnTime;

    /**
     * 审批人（系统用户ID）
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 驳回理由
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 状态（0：待审批，1：请假中，2：已销假，3：已驳回）
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
     * 逻辑删除（1：已删除，0：未删除）
     */
    @TableLogic
    private Integer deleted;


}
