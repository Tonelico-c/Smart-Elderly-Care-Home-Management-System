package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ExamItemMapper;
import com.situ.elder.pojo.entity.ExamItem;
import com.situ.elder.pojo.query.ExamItemQuery;
import com.situ.elder.service.IExamItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 体检项目表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@Service
public class ExamItemServiceImpl extends ServiceImpl<ExamItemMapper, ExamItem> implements IExamItemService {

    @Autowired
    private ExamItemMapper examItemMapper;

    @Override
    public IPage<ExamItem> list(ExamItemQuery examItemQuery) {
        IPage<ExamItem> page = new Page<>(examItemQuery.getPage(), examItemQuery.getLimit());
        LambdaQueryWrapper<ExamItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(examItemQuery.getName()),ExamItem::getName, examItemQuery.getName())
                .eq(!ObjectUtils.isEmpty(examItemQuery.getStatus()),ExamItem::getStatus, examItemQuery.getStatus())
                .between(!ObjectUtils.isEmpty(examItemQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(examItemQuery.getEndCreateTime()), ExamItem::getCreateTime, examItemQuery.getBeginCreateTime(), examItemQuery.getEndCreateTime())
                .orderByAsc(ExamItem::getSort);
        return examItemMapper.selectPage(page, lambdaQueryWrapper);
    }
}
