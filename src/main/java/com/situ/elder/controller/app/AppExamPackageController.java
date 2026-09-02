package com.situ.elder.controller.app;

import com.situ.elder.pojo.vo.ExamPackageVO;
import com.situ.elder.service.IExamPackageService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 体检套餐表 前端控制器（app端）
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/app/exam-packages")
public class AppExamPackageController {

    @Autowired
    private IExamPackageService examPackageService;

    /**
     * 上架的套餐列表（含每个套餐的项目数）
     */
    @GetMapping
    public Result<List<ExamPackageVO>> list(){
        List<ExamPackageVO> list = examPackageService.listOnShelf();
        return Result.ok(list);
    }

    /**
     * 套餐详情（含包含的体检项目明细）
     */
    @GetMapping("/{id}")
    public Result<ExamPackageVO> selectById(@PathVariable Long id){
        ExamPackageVO examPackageVO = examPackageService.getDetail(id);
        return Result.ok(examPackageVO);
    }
}
