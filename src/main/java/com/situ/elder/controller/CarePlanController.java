package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CarePlan;
import com.situ.elder.pojo.query.CarePlanQuery;
import com.situ.elder.service.ICarePlanService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 护理计划表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/care-plans")
public class CarePlanController {

    @Autowired
    private ICarePlanService carePlanService;

    @GetMapping
    public Result<IPage<CarePlan>> list(CarePlanQuery carePlanQuery){
        IPage<CarePlan> page = carePlanService.list(carePlanQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<CarePlan> selectById(@PathVariable Long id){
        CarePlan carePlan = carePlanService.getPlanById(id);
        return Result.ok(carePlan);
    }

    @PostMapping
    public Result<CarePlan> add(@RequestBody CarePlan carePlan){
        carePlanService.savePlan(carePlan);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody CarePlan carePlan){
        carePlanService.updatePlan(carePlan);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        carePlanService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        carePlanService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
