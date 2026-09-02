package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.ExamItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 体检套餐VO（app端）：套餐信息 + 包含项目数 + 项目明细
 */
@Data
public class ExamPackageVO {

    /**
     * 体检套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐价格
     */
    private BigDecimal price;

    /**
     * 套餐图片
     */
    private String image;

    /**
     * 套餐说明
     */
    private String description;

    /**
     * 状态：0下架 1上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 包含项目数（列表用）
     */
    private Integer itemCount;

    /**
     * 包含的体检项目明细（详情用）
     */
    private List<ExamItem> examItemList;
}
