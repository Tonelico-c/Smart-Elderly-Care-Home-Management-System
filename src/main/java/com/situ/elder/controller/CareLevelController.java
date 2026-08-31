package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareLevel;
import com.situ.elder.pojo.query.CareLevelQuery;
import com.situ.elder.service.ICareLevelService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 护理等级表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-31
 */
@RestController
@RequestMapping("/care-levels")
public class CareLevelController {

    @Autowired
    private ICareLevelService careLevelService;

    @GetMapping
    public Result<IPage<CareLevel>> list(CareLevelQuery careLevelQuery){
        IPage<CareLevel> page = careLevelService.list(careLevelQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<CareLevel> selectById(@PathVariable Long id){
        CareLevel careLevel = careLevelService.getById(id);
        return Result.ok(careLevel);
    }

    @PostMapping
    public Result<CareLevel> add(@RequestBody CareLevel careLevel){
        careLevelService.save(careLevel);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody CareLevel careLevel){
        careLevelService.updateById(careLevel);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        careLevelService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        careLevelService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
