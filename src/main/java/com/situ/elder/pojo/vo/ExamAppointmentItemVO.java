package com.situ.elder.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 体检记录明细视图对象（管理后台）
 * <p>
 * 在体检记录明细的基础上，补充体检项目的结果类型、单位、参考范围等信息，
 * 用于后台录入或查看体检结果。
 */
@Data
public class ExamAppointmentItemVO {
    /**
     * 体检记录明细ID
     */
    private Long id;

    /**
     * 体检记录ID
     */
    private Long appointmentId;

    /**
     * 体检项目ID
     */
    private Long examItemId;

    /**
     * 项目名称快照
     */
    private String itemName;

    /**
     * 数值型结果
     */
    private BigDecimal resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 文本型结果
     */
    private String resultText;

    /**
     * 状态：0待检查 1正常 2异常 3未完成
     */
    private Integer status;

    /**
     * 是否异常：0正常 1异常
     */
    private Integer abnormal;

    /**
     * 备注
     */
    private String remark;

    /**
     * 结果类型：0文本 1数值
     */
    private Integer resultType;

    /**
     * 项目单位
     */
    private String unit;

    /**
     * 参考范围下限
     */
    private BigDecimal referenceMin;

    /**
     * 参考范围上限
     */
    private BigDecimal referenceMax;

    /**
     * 参考范围单位
     */
    private String referenceUnit;
}
