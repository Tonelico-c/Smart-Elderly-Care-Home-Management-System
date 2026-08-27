package com.situ.elder.pojo.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.situ.elder.pojo.entity.Elder;

import com.situ.elder.pojo.entity.Tag;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class ElderVo{
    /**
     * 老人ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 老人姓名
     */
    private String name;

    /**
     * 密码哈希
     */
    private String password;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 身份证号
     */
    @TableField("id_card_no")
    private String idCardNo;

    /**
     * 状态（0：禁用，1：启用，2：请假，3：退住中，4：入住中，5：已退住）
     */
    private Integer status;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    /**
     * 家庭住址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除（0：未删除，1：已删除）
     */
    @TableLogic
    private Integer deleted;

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



    private List<Tag> tags;

    private List<String> tagNames;

    private String tagNamesStr;
}
