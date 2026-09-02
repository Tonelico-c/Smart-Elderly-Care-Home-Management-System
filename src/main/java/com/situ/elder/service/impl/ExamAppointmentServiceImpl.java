package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ExamAppointmentMapper;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.entity.ExamPackageItem;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IElderService;
import com.situ.elder.service.IExamAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.service.IExamPackageItemService;
import com.situ.elder.service.IExamPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ExamAppointmentMapper examAppointmentMapper;
    @Autowired
    private IElderService elderService;
    @Autowired
    private IExamPackageService examPackageService;
    @Autowired
    private IExamPackageItemService examPackageItemService;



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

    @Override
    public List<ExamAppointmentVO> listByElderId(Long elderId) {
        List<ExamAppointment> examAppointmentList = lambdaQuery()
                .eq(ExamAppointment::getElderId, elderId)
                .orderByDesc(ExamAppointment::getAppointmentDate)
                .orderByDesc(ExamAppointment::getAppointmentTime)
                .list();

        Elder elder = elderService.getById(elderId);
        String elderName = elder != null ? elder.getName() : null;

        List<Long> packageIds = examAppointmentList.stream()
                .map(ExamAppointment::getPackageId)
                .distinct()
                .toList();

        Map<Long, String> packageNameMap = examPackageService.listByIds(packageIds).stream()
                .collect(Collectors.toMap(ExamPackage::getId, ExamPackage::getName));

        //批量统计每个套餐的项目数量
        Map<Long, Long> itemCountMap = examPackageItemService.lambdaQuery()
                .in(ExamPackageItem::getPackageId, packageIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(ExamPackageItem::getPackageId, Collectors.counting()));

        return examAppointmentList.stream().map(examAppointment -> {
            ExamAppointmentVO examAppointmentVO = new ExamAppointmentVO();
            examAppointmentVO.setId(examAppointment.getId());
            examAppointmentVO.setPackageId(examAppointment.getPackageId());
            examAppointmentVO.setPackageName(packageNameMap.getOrDefault(examAppointment.getPackageId(), "已删除套餐"));
            examAppointmentVO.setElderName(elderName);
            examAppointmentVO.setAppointmentDate(examAppointment.getAppointmentDate().toString());
            examAppointmentVO.setAppointmentTime(examAppointment.getAppointmentTime().format(TIME_FORMATTER));
            examAppointmentVO.setPrice(examAppointment.getPrice());
            examAppointmentVO.setStatus(examAppointment.getStatus());
            examAppointmentVO.setExamItemCount(itemCountMap.getOrDefault(examAppointment.getPackageId(), 0L).intValue());
            return examAppointmentVO;
        }).toList();
    }
}
