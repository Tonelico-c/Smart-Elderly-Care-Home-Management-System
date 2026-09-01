package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.CareItemMapper;
import com.situ.elder.mapper.CarePlanItemMapper;
import com.situ.elder.mapper.CarePlanMapper;
import com.situ.elder.pojo.entity.CareItem;
import com.situ.elder.pojo.entity.CarePlan;
import com.situ.elder.pojo.entity.CarePlanItem;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CarePlanQuery;
import com.situ.elder.service.ICarePlanService;
import com.situ.elder.service.ICareTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 护理计划表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class CarePlanServiceImpl extends ServiceImpl<CarePlanMapper, CarePlan> implements ICarePlanService {

    @Autowired
    private CarePlanMapper carePlanMapper;

    @Autowired
    private CarePlanItemMapper carePlanItemMapper;

    @Autowired
    private CareItemMapper careItemMapper;

    @Autowired
    private ICareTaskService careTaskService;

    @Override
    public IPage<CarePlan> list(CarePlanQuery carePlanQuery) {
        IPage<CarePlan> page = new Page<>(carePlanQuery.getPage(), carePlanQuery.getLimit());
        LambdaQueryWrapper<CarePlan> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(carePlanQuery.getName()),CarePlan::getName, carePlanQuery.getName())
                .eq(!ObjectUtils.isEmpty(carePlanQuery.getStatus()),CarePlan::getStatus, carePlanQuery.getStatus())
                .between(!ObjectUtils.isEmpty(carePlanQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(carePlanQuery.getEndCreateTime()), CarePlan::getCreateTime, carePlanQuery.getBeginCreateTime(), carePlanQuery.getEndCreateTime())
                .orderByDesc(CarePlan::getCreateTime);
        return carePlanMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    @Transactional
    public void savePlan(CarePlan carePlan) {
        carePlanMapper.insert(carePlan);
        saveItems(carePlan);
        generateTasks(carePlan);
    }

    @Override
    @Transactional
    public void updatePlan(CarePlan carePlan) {
        carePlanMapper.updateById(carePlan);
        // 先删除原有明细，再保存新明细
        LambdaQueryWrapper<CarePlanItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarePlanItem::getCarePlanId, carePlan.getId());
        carePlanItemMapper.delete(wrapper);
        saveItems(carePlan);
    }

    @Override
    public CarePlan getPlanById(Long id) {
        CarePlan carePlan = carePlanMapper.selectById(id);
        if (carePlan != null) {
            LambdaQueryWrapper<CarePlanItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CarePlanItem::getCarePlanId, id);
            carePlan.setItems(carePlanItemMapper.selectList(wrapper));
        }
        return carePlan;
    }

    private void saveItems(CarePlan carePlan) {
        if (CollectionUtils.isEmpty(carePlan.getItems())) {
            return;
        }
        for (CarePlanItem item : carePlan.getItems()) {
            item.setId(null);
            item.setCarePlanId(carePlan.getId());
            carePlanItemMapper.insert(item);
        }
    }

    /**
     * 根据护理计划明细生成护理任务（care_task），按执行周期在计划起止日期内展开
     */
    private void generateTasks(CarePlan carePlan) {
        if (carePlan.getStartDate() == null || carePlan.getEndDate() == null
                || CollectionUtils.isEmpty(carePlan.getItems())) {
            return;
        }
        // 查询护理项目名称，冗余存入 care_task，防止项目改名影响历史任务
        List<Long> careItemIds = carePlan.getItems().stream()
                .map(CarePlanItem::getCareItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, String> careItemNameMap = careItemIds.isEmpty() ? Collections.emptyMap()
                : careItemMapper.selectBatchIds(careItemIds).stream()
                .collect(Collectors.toMap(CareItem::getId, CareItem::getName, (a, b) -> a));

        List<CareTask> tasks = new ArrayList<>();
        for (CarePlanItem item : carePlan.getItems()) {
            if (item.getCareItemId() == null || item.getExecuteTime() == null) {
                continue;
            }
            int frequency = item.getExecuteFrequency() == null ? 1 : item.getExecuteFrequency();
            for (Date date : buildExecuteDates(carePlan.getStartDate(), carePlan.getEndDate(), item.getExecuteCycle())) {
                for (int i = 0; i < frequency; i++) {
                    CareTask task = new CareTask();
                    task.setElderId(carePlan.getElderId());
                    task.setCarePlanId(carePlan.getId());
                    task.setCareItemId(item.getCareItemId());
                    task.setCareItemName(careItemNameMap.get(item.getCareItemId()));
                    task.setUserId(carePlan.getUserId());
                    task.setPlanExecuteDate(date);
                    task.setPlanExecuteTime(item.getExecuteTime());
                    task.setStatus(0);
                    tasks.add(task);
                }
            }
        }
        if (!tasks.isEmpty()) {
            careTaskService.saveBatch(tasks);
        }
    }

    /**
     * 按执行周期（0每天 1每周 2每月）在计划起止日期内生成执行日期
     */
    private List<Date> buildExecuteDates(Date startDate, Date endDate, Integer executeCycle) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        while (!calendar.after(endCalendar)) {
            dates.add(calendar.getTime());
            if (executeCycle != null && executeCycle == 1) {
                calendar.add(Calendar.DAY_OF_MONTH, 7);
            } else if (executeCycle != null && executeCycle == 2) {
                calendar.add(Calendar.MONTH, 1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return dates;
    }
}
