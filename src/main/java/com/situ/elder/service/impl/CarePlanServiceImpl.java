package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.CarePlanItemMapper;
import com.situ.elder.mapper.CarePlanMapper;
import com.situ.elder.pojo.entity.CarePlan;
import com.situ.elder.pojo.entity.CarePlanItem;
import com.situ.elder.pojo.query.CarePlanQuery;
import com.situ.elder.service.ICarePlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
}
