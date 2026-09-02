package com.situ.elder.mapper;

import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.vo.ExamPackageVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 体检套餐表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
public interface ExamPackageMapper extends BaseMapper<ExamPackage> {

    /**
     * 查询上架的套餐列表（含每个套餐的项目数）
     */
    List<ExamPackageVO> listOnShelf();

    /**
     * 查询套餐详情（含包含的体检项目明细）
     */
    ExamPackageVO selectDetail(Long id);
}
