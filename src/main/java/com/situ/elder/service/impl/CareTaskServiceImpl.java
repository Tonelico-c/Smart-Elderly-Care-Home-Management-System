package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.CareTaskMapper;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CareTaskQuery;
import com.situ.elder.service.ICareTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 护理任务与打卡记录表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class CareTaskServiceImpl extends ServiceImpl<CareTaskMapper, CareTask> implements ICareTaskService {

    @Autowired
    private CareTaskMapper careTaskMapper;

    @Override
    public IPage<CareTask> list(CareTaskQuery careTaskQuery) {
        IPage<CareTask> page = new Page<>(careTaskQuery.getPage(), careTaskQuery.getLimit());
        LambdaQueryWrapper<CareTask> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(careTaskQuery.getElderId()), CareTask::getElderId, careTaskQuery.getElderId())
                .eq(!ObjectUtils.isEmpty(careTaskQuery.getStatus()), CareTask::getStatus, careTaskQuery.getStatus())
                .between(!ObjectUtils.isEmpty(careTaskQuery.getBeginPlanExecuteDate()) && !ObjectUtils.isEmpty(careTaskQuery.getEndPlanExecuteDate()), CareTask::getPlanExecuteDate, careTaskQuery.getBeginPlanExecuteDate(), careTaskQuery.getEndPlanExecuteDate())
                .orderByDesc(CareTask::getCreateTime);
        return careTaskMapper.selectPage(page, lambdaQueryWrapper);
    }
}
