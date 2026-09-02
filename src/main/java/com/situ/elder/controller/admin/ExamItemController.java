package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamItem;
import com.situ.elder.pojo.query.ExamItemQuery;
import com.situ.elder.service.IExamItemService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 体检项目表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/admin/exam-items")
public class ExamItemController {

    @Autowired
    private IExamItemService examItemService;

    @GetMapping
    public Result<IPage<ExamItem>> list(ExamItemQuery examItemQuery){
        IPage<ExamItem> page = examItemService.list(examItemQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<ExamItem> selectById(@PathVariable Long id){
        ExamItem examItem = examItemService.getById(id);
        return Result.ok(examItem);
    }

    @PostMapping
    public Result<ExamItem> add(@RequestBody ExamItem examItem){
        examItemService.save(examItem);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody ExamItem examItem){
        examItemService.updateById(examItem);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        examItemService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        examItemService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
