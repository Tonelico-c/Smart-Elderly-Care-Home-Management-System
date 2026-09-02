package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ExamPackageMapper;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.query.ExamPackageQuery;
import com.situ.elder.service.IExamPackageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 体检套餐表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@Service
public class ExamPackageServiceImpl extends ServiceImpl<ExamPackageMapper, ExamPackage> implements IExamPackageService {

    @Autowired
    private ExamPackageMapper examPackageMapper;

    @Override
    public IPage<ExamPackage> list(ExamPackageQuery examPackageQuery) {
        IPage<ExamPackage> page = new Page<>(examPackageQuery.getPage(), examPackageQuery.getLimit());
        LambdaQueryWrapper<ExamPackage> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(examPackageQuery.getName()),ExamPackage::getName, examPackageQuery.getName())
                .eq(!ObjectUtils.isEmpty(examPackageQuery.getStatus()),ExamPackage::getStatus, examPackageQuery.getStatus())
                .between(!ObjectUtils.isEmpty(examPackageQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(examPackageQuery.getEndCreateTime()), ExamPackage::getCreateTime, examPackageQuery.getBeginCreateTime(), examPackageQuery.getEndCreateTime())
                .orderByAsc(ExamPackage::getSort);
        return examPackageMapper.selectPage(page, lambdaQueryWrapper);
    }
}
