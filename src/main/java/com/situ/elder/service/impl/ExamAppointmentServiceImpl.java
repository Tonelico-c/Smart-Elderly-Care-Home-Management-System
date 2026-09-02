package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ExamAppointmentMapper;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.situ.elder.service.IExamAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 老人预约/体检记录表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@Service
public class ExamAppointmentServiceImpl extends ServiceImpl<ExamAppointmentMapper, ExamAppointment> implements IExamAppointmentService {

    @Autowired
    private ExamAppointmentMapper examAppointmentMapper;

    @Override
    public IPage<ExamAppointment> list(ExamAppointmentQuery examAppointmentQuery) {
        IPage<ExamAppointment> page = new Page<>(examAppointmentQuery.getPage(), examAppointmentQuery.getLimit());
        LambdaQueryWrapper<ExamAppointment> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(examAppointmentQuery.getElderId()),ExamAppointment::getElderId, examAppointmentQuery.getElderId())
                .eq(!ObjectUtils.isEmpty(examAppointmentQuery.getPackageId()),ExamAppointment::getPackageId, examAppointmentQuery.getPackageId())
                .eq(!ObjectUtils.isEmpty(examAppointmentQuery.getStatus()),ExamAppointment::getStatus, examAppointmentQuery.getStatus())
                .between(!ObjectUtils.isEmpty(examAppointmentQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(examAppointmentQuery.getEndCreateTime()), ExamAppointment::getCreateTime, examAppointmentQuery.getBeginCreateTime(), examAppointmentQuery.getEndCreateTime())
                .orderByDesc(ExamAppointment::getCreateTime);
        return examAppointmentMapper.selectPage(page, lambdaQueryWrapper);
    }
}
