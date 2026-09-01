package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CareTaskQuery;
import com.situ.elder.service.ICareTaskService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 护理任务与打卡记录表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/care-tasks")
public class CareTaskController {

    @Autowired
    private ICareTaskService careTaskService;

    @GetMapping
    public Result<IPage<CareTask>> list(CareTaskQuery careTaskQuery){
        IPage<CareTask> page = careTaskService.list(careTaskQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<CareTask> selectById(@PathVariable Long id){
        CareTask careTask = careTaskService.getById(id);
        return Result.ok(careTask);
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody CareTask careTask){
        careTaskService.updateById(careTask);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        careTaskService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        careTaskService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
