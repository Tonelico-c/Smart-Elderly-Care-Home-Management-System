package com.situ.elder.pojo.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
public class ElderLeaveVO {
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
    /**
     * 老人姓名
     */
    private String  elderName;

    /**
     * 审批人姓名
     */
    private String approverName;
}
