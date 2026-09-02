package com.situ.elder.service;

import com.situ.elder.pojo.entity.ExamPackageItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 体检套餐项目关联表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
public interface IExamPackageItemService extends IService<ExamPackageItem> {

    /**
     * 查询某个套餐已分配的体检项目id列表
     */
    List<Long> selectAssignedItem(Long packageId);

    /**
     * 给套餐分配体检项目:先删除原有关联,再插入新的
     */
    void assignItem(Long packageId, Long[] examItemIds);
}
