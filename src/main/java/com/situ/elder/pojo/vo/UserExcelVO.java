package com.situ.elder.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户导入/导出用的 Excel 行对象。
 * <p>
 * 注意：这里**不能**出现 password、deleted 这类字段——
 * 导出走的是全表查询 + 同名属性拷贝，多一个字段就等于把密码哈希写进了 Excel 文件；
 * 导入模板也不应该携带密码哈希（没法校验合法性），导入用户的初始密码在监听器里统一设置。
 */
@Data
public class UserExcelVO {
    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long id;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名")
    private String name;

    /**
     * 手机号
     */
    @ExcelProperty(value = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @ExcelProperty(value = "邮箱")
    private String email;

    /**
     * 头像URL
     */
    @ExcelProperty(value = "头像URL")
    private String avatar;

    /**
     * 状态（0：停用，1：正常）
     */
    @ExcelProperty(value = "状态（0：停用，1：正常）")
    private Integer status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

}
