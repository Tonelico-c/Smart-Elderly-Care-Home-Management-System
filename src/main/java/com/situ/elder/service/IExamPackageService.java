package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.query.ExamPackageQuery;
import com.situ.elder.pojo.vo.ExamPackageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 体检套餐表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
public interface IExamPackageService extends IService<ExamPackage> {

    IPage<ExamPackage> list(ExamPackageQuery examPackageQuery);

    /**
     * 查询上架的套餐列表（app端，含项目数）
     */
    List<ExamPackageVO> listOnShelf();

    /**
     * 查询套餐详情（app端，含包含的体检项目明细）
     */
    ExamPackageVO getDetail(Long id);
}
