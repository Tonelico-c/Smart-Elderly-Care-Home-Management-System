package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamPackage;
import com.situ.elder.pojo.query.ExamPackageQuery;
import com.situ.elder.service.IExamPackageService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 体检套餐表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/admin/exam-packages")
public class ExamPackageController {

    @Autowired
    private IExamPackageService examPackageService;

    @GetMapping
    public Result<IPage<ExamPackage>> list(ExamPackageQuery examPackageQuery){
        IPage<ExamPackage> page = examPackageService.list(examPackageQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<ExamPackage> selectById(@PathVariable Long id){
        ExamPackage examPackage = examPackageService.getById(id);
        return Result.ok(examPackage);
    }

    @PostMapping
    public Result<ExamPackage> add(@RequestBody ExamPackage examPackage){
        examPackageService.save(examPackage);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody ExamPackage examPackage){
        examPackageService.updateById(examPackage);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        examPackageService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        examPackageService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
