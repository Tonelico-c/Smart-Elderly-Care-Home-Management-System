package com.situ.elder.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ElderExcelVO {

    @ExcelProperty(value = "老人ID")
    private Long id;

    @ExcelProperty(value = "老人姓名")
    private  String name;

    @ExcelProperty(value = "身份证号")
    @TableField("id_card_no")
    private String idCardNo;

    @ExcelProperty(value = "状态")
    private Integer status;

    @ExcelProperty(value = "电话")
    private String phone;

    @ExcelProperty(value = "出生日期")
    @DateTimeFormat("yyyy-MM-dd")
    private Date birthday;

    @ExcelProperty(value = "家庭住址")
    private String address;

    @ExcelProperty(value = "标签")
    private String tagNamesStr;

    @ExcelProperty(value = "头像URL")
    private String avatar;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    @ExcelProperty(value = "状态备注", index=4)
    private String statusRemark;
}
