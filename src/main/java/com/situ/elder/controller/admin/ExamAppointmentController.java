package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 老人预约/体检记录表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/admin/exam-appointments")
public class ExamAppointmentController {

    @Autowired
    private IExamAppointmentService examAppointmentService;

    @GetMapping
    public Result<IPage<ExamAppointment>> list(ExamAppointmentQuery examAppointmentQuery){
        IPage<ExamAppointment> page = examAppointmentService.list(examAppointmentQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<ExamAppointment> selectById(@PathVariable Long id){
        ExamAppointment examAppointment = examAppointmentService.getById(id);
        return Result.ok(examAppointment);
    }

    @PostMapping
    public Result<ExamAppointment> add(@RequestBody ExamAppointment examAppointment){
        examAppointmentService.save(examAppointment);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody ExamAppointment examAppointment){
        examAppointmentService.updateById(examAppointment);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        examAppointmentService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        examAppointmentService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
