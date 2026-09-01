package com.situ.elder.service;

import com.situ.elder.pojo.entity.CarePlan;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.CarePlanQuery;

/**
 * <p>
 * 护理计划表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface ICarePlanService extends IService<CarePlan> {

    IPage<CarePlan> list(CarePlanQuery carePlanQuery);

    void savePlan(CarePlan carePlan);

    void updatePlan(CarePlan carePlan);

    CarePlan getPlanById(Long id);
}
