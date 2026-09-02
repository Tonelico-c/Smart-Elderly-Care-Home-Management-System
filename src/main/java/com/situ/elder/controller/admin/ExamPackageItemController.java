package com.situ.elder.controller.admin;


import com.situ.elder.service.IExamPackageItemService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 体检套餐项目关联表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/admin/exam-package-item")
public class ExamPackageItemController {

    @Autowired
    private IExamPackageItemService examPackageItemService;

    /**
     * 查询套餐已分配的体检项目id列表
     * GET /admin/exam-package-item/selectAssignedItem/1
     */
    @GetMapping("/selectAssignedItem/{packageId}")
    public Result<List<Long>> selectAssignedItem(@PathVariable Long packageId){
        List<Long> examItemIds = examPackageItemService.selectAssignedItem(packageId);
        return Result.ok(examItemIds);
    }

    /**
     * 给套餐分配体检项目:先删除原有关联,再插入新的
     * POST /admin/exam-package-item/assignItem?packageId=1&examItemIds=1,2,3
     */
    @PostMapping("/assignItem")
    public Result assignItem(Long packageId, Long[] examItemIds){
        examPackageItemService.assignItem(packageId, examItemIds);
        return Result.ok("分配成功");
    }
}
