package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.ExamAppointmentMapper;
import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.entity.*;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    @Autowired
    private IExamItemService examItemService;
    @Autowired
    private IExamAppointmentItemService examAppointmentItemService;


    //分页查询预约/体检记录
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

    //根据老人id查询预约/体检记录
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
    //添加预约/体检记录
    @Override
    public void add(AppAppointmentDTO appAppointmentDTO, Long elderId) {
        //解析并校验日期、时间
        LocalDate appointmentDate;
        LocalTime appointmentTime;
        try {
            appointmentDate = LocalDate.parse(appAppointmentDTO.getDate());
            appointmentTime = LocalTime.parse(appAppointmentDTO.getTime());
        } catch (Exception e) {
            throw new ServiceException("预约日期或时间格式不正确");
        }
        if (LocalDateTime.of(appointmentDate, appointmentTime).isBefore(LocalDateTime.now())) {
            throw new ServiceException("预约时间必须晚于当前时间");
        }

        //校验套餐存在且上架
        ExamPackage examPackage = examPackageService.getById(appAppointmentDTO.getPackageId());
        if (examPackage == null) {
            throw new ServiceException("体检套餐不存在");
        }
        if (examPackage.getStatus() != 1) {
            throw new ServiceException("体检套餐已下架，无法预约");
        }

        //同一老人同一时段不能重复预约
        Long count = lambdaQuery()
                .eq(ExamAppointment::getElderId, elderId)
                .eq(ExamAppointment::getAppointmentDate, appointmentDate)
                .eq(ExamAppointment::getAppointmentTime, appointmentTime)
                .in(ExamAppointment::getStatus, 0, 1)
                .count();
        if (count > 0) {
            throw new ServiceException("该时间段您已有预约，请选择其他时间");
        }

        //保存预约（价格取套餐当前价格快照）
        ExamAppointment examAppointment = new ExamAppointment();
        examAppointment.setElderId(elderId);
        examAppointment.setPackageId(examPackage.getId());
        examAppointment.setAppointmentDate(appointmentDate);
        examAppointment.setAppointmentTime(appointmentTime);
        examAppointment.setPrice(examPackage.getPrice());
        examAppointment.setStatus(0);
        save(examAppointment);

        //写入套餐内项目的快照，后续体检结果直接录到这些明细上
        List<ExamPackageItem> examPackageItemList = examPackageItemService.lambdaQuery()
                .eq(ExamPackageItem::getPackageId, examPackage.getId())
                .orderByAsc(ExamPackageItem::getSort)
                .list();
        if (ObjectUtils.isEmpty(examPackageItemList)) {
            return;
        }
        List<Long> examItemIdList = examPackageItemList.stream().map(ExamPackageItem::getExamItemId).toList();
        Map<Long, ExamItem> examItemMap = examItemService.listByIds(examItemIdList).stream()
                .collect(Collectors.toMap(ExamItem::getId, Function.identity()));
        List<ExamAppointmentItem> examAppointmentItemList = new ArrayList<>();
        for (ExamPackageItem examPackageItem : examPackageItemList) {
            ExamItem examItem = examItemMap.get(examPackageItem.getExamItemId());
            if (examItem == null) {
                continue;
            }
            ExamAppointmentItem examAppointmentItem = new ExamAppointmentItem();
            examAppointmentItem.setAppointmentId(examAppointment.getId());
            examAppointmentItem.setExamItemId(examItem.getId());
            examAppointmentItem.setItemName(examItem.getName());
            examAppointmentItem.setStatus(0);
            examAppointmentItem.setAbnormal(0);
            examAppointmentItemList.add(examAppointmentItem);
        }
        examAppointmentItemService.saveBatch(examAppointmentItemList);
    }

    @Override
    public void cancel(Long id, Long elderId) {
        ExamAppointment examAppointment = getById(id);
        if (examAppointment == null) {
            throw new ServiceException("预约不存在");
        }
        //只能取消自己的预约
        if (!examAppointment.getElderId().equals(elderId)) {
            throw new ServiceException("无权取消他人的预约");
        }
        //只有待体检的预约才能取消
        if (examAppointment.getStatus() != 0) {
            throw new ServiceException("当前状态不允许取消");
        }

        ExamAppointment update = new ExamAppointment();
        update.setId(id);
        update.setStatus(3);
        updateById(update);
    }
}
