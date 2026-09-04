package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.ExamAppointmentMapper;
import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.entity.*;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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


    /**
     * 分页查询预约/体检记录（管理后台）
     * <p>
     * 按查询条件动态拼接 SQL：老人 id、套餐 id、状态非空时才作为过滤条件，
     * 创建时间区间两端都传了才按区间过滤，最后按创建时间倒序排列。
     *
     * @param examAppointmentQuery 列表查询条件（含分页参数）
     * @return 分页后的预约/体检记录
     */
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

    /**
     * 根据老人 id 查询其所有预约/体检记录（App 端）
     * <p>
     * 记录按预约日期、预约时间倒序排列，返回前批量补齐展示信息，避免循环查库：
     * 先查出老人的姓名，再一次性查出所有涉及套餐的名称映射，
     * 以及每个套餐包含的体检项目数量，最后逐条组装为视图对象。
     * 套餐已被删除时名称显示为"已删除套餐"。
     *
     * @param elderId 老人 id
     * @return 该老人的预约记录视图对象列表（含套餐名称、老人姓名、项目数量）
     */
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
    /**
     * 老人新增体检预约（App 端）
     * <p>
     * 整体流程：
     * <ol>
     *     <li>解析并校验预约日期时间格式，且预约时间必须晚于当前时间；</li>
     *     <li>校验套餐存在且处于上架状态（status = 1），下架套餐不可预约；</li>
     *     <li>校验同一老人在同一天同一时段不存在待体检/体检中的预约，防止重复预约；</li>
     *     <li>保存预约记录：价格取套餐当前价格作为快照（避免套餐调价影响历史预约），
     *         状态置为待体检（0）；</li>
     *     <li>按套餐-项目关联表查出套餐包含的体检项目，为每个项目写入一条预约明细快照，
     *         冗余保存项目名称，后续体检结果直接录入到这些明细上；
     *         项目本身已被删除的跳过，套餐没有有效项目时直接返回。</li>
     * </ol>
     *
     * @param appAppointmentDTO 预约信息（套餐 id、日期 yyyy-MM-dd、时间 HH:mm）
     * @param elderId           老人 id（从 token 中解析，不信任前端传入）
     * @throws ServiceException 日期时间格式不正确、时间已过期、套餐不存在或已下架、
     *                          同一时段已有预约时抛出
     */
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

    /**
     * 老人取消体检预约（App 端）
     * <p>
     * 取消前依次校验：预约存在、预约属于当前老人（不能取消他人的预约）、
     * 预约状态为待体检（0）——已体检中/已完成/已取消的预约不允许再取消。
     * 校验通过后将状态更新为已取消（3）。
     *
     * @param id       预约记录 id
     * @param elderId  当前登录老人 id（从 token 中解析，用于归属校验）
     * @throws ServiceException 预约不存在、无权取消他人预约或状态不允许取消时抛出
     */
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

    /**
     * 查询预约的体检项目明细（管理后台）
     * <p>
     * 整体流程：
     * <ol>
     *     <li>校验预约存在；</li>
     *     <li>查询该预约的明细快照，若没有明细（如后台直接添加的预约），
     *         则按套餐-项目关联表补一份快照并保存，保证后续结果可录入；</li>
     *     <li>批量查询涉及的体检项目，为每条明细补充结果类型、单位、参考范围后返回。</li>
     * </ol>
     *
     * @param id 预约 id
     * @return 体检项目明细视图对象列表
     * @throws ServiceException 预约不存在时抛出
     */
    @Override
    public List<ExamAppointmentItemVO> listItems(Long id) {
        ExamAppointment examAppointment = getById(id);
        if (examAppointment == null) {
            throw new ServiceException("预约不存在");
        }

        List<ExamAppointmentItem> examAppointmentItemList = examAppointmentItemService.lambdaQuery()
                .eq(ExamAppointmentItem::getAppointmentId, id)
                .orderByAsc(ExamAppointmentItem::getId)
                .list();

        //没有明细快照时（后台直接添加的预约），按套餐补一份快照
        if (ObjectUtils.isEmpty(examAppointmentItemList)) {
            examAppointmentItemList = createItemsFromPackage(examAppointment);
        }
        if (ObjectUtils.isEmpty(examAppointmentItemList)) {
            return List.of();
        }

        //批量查询体检项目，补充结果类型、单位、参考范围
        List<Long> examItemIdList = examAppointmentItemList.stream()
                .map(ExamAppointmentItem::getExamItemId)
                .distinct()
                .toList();
        Map<Long, ExamItem> examItemMap = examItemService.listByIds(examItemIdList).stream()
                .collect(Collectors.toMap(ExamItem::getId, Function.identity()));

        return examAppointmentItemList.stream().map(examAppointmentItem -> {
            ExamAppointmentItemVO vo = new ExamAppointmentItemVO();
            vo.setId(examAppointmentItem.getId());
            vo.setAppointmentId(examAppointmentItem.getAppointmentId());
            vo.setExamItemId(examAppointmentItem.getExamItemId());
            vo.setItemName(examAppointmentItem.getItemName());
            vo.setResultValue(examAppointmentItem.getResultValue());
            vo.setResultUnit(examAppointmentItem.getResultUnit());
            vo.setResultText(examAppointmentItem.getResultText());
            vo.setStatus(examAppointmentItem.getStatus());
            vo.setAbnormal(examAppointmentItem.getAbnormal());
            vo.setRemark(examAppointmentItem.getRemark());
            ExamItem examItem = examItemMap.get(examAppointmentItem.getExamItemId());
            if (examItem != null) {
                vo.setResultType(examItem.getResultType());
                vo.setUnit(examItem.getUnit());
                vo.setReferenceMin(examItem.getReferenceMin());
                vo.setReferenceMax(examItem.getReferenceMax());
                vo.setReferenceUnit(examItem.getReferenceUnit());
            }
            return vo;
        }).toList();
    }

    /**
     * 保存体检结果（管理后台）
     * <p>
     * 逐条更新明细的结果数据：按是否异常设置明细状态（1正常 2异常）。
     * 待体检/体检中的预约保存后状态置为已完成（2）；
     * 已完成的预约再次保存视为修改，直接更新明细数据，状态保持已完成不变。
     * 已取消/已过期的预约不允许录入结果。
     *
     * @param id       预约 id
     * @param itemList 体检项目明细结果列表
     * @throws ServiceException 预约不存在或已取消/已过期时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveResults(Long id, List<ExamAppointmentItem> itemList) {
        ExamAppointment examAppointment = getById(id);
        if (examAppointment == null) {
            throw new ServiceException("预约不存在");
        }
        //已取消/已过期的预约不允许录入结果；已完成（2）的预约允许修改
        if (examAppointment.getStatus() == 3 || examAppointment.getStatus() == 4) {
            throw new ServiceException("该预约已取消或已过期，不能录入体检结果");
        }

        for (ExamAppointmentItem item : itemList) {
            ExamAppointmentItem update = new ExamAppointmentItem();
            update.setId(item.getId());
            update.setResultValue(item.getResultValue());
            update.setResultText(item.getResultText());
            update.setAbnormal(item.getAbnormal() == null ? 0 : item.getAbnormal());
            //按是否异常设置明细状态：1正常 2异常
            update.setStatus(item.getAbnormal() != null && item.getAbnormal() == 1 ? 2 : 1);
            update.setRemark(item.getRemark());
            examAppointmentItemService.updateById(update);
        }

        //待体检/体检中的预约保存结果后状态改为已完成；已完成的预约保持原状态（修改数据）
        if (examAppointment.getStatus() != 2) {
            ExamAppointment update = new ExamAppointment();
            update.setId(id);
            update.setStatus(2);
            updateById(update);
        }
    }

    /**
     * 按套餐-项目关联表生成预约明细快照并保存
     *
     * @param examAppointment 预约记录
     * @return 生成的明细快照列表（套餐无有效项目时返回空列表）
     */
    private List<ExamAppointmentItem> createItemsFromPackage(ExamAppointment examAppointment) {
        // 查询套餐项目关联表
        List<ExamPackageItem> examPackageItemList = examPackageItemService.lambdaQuery()
                .eq(ExamPackageItem::getPackageId, examAppointment.getPackageId())
                .orderByAsc(ExamPackageItem::getSort)
                .list();
        if (ObjectUtils.isEmpty(examPackageItemList)) {
            return List.of();
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
            examAppointmentItem.setResultUnit(examItem.getUnit());
            examAppointmentItem.setStatus(0);
            examAppointmentItem.setAbnormal(0);
            examAppointmentItemList.add(examAppointmentItem);
        }
        if (!examAppointmentItemList.isEmpty()) {
            examAppointmentItemService.saveBatch(examAppointmentItemList);
        }
        return examAppointmentItemList;
    }
}
