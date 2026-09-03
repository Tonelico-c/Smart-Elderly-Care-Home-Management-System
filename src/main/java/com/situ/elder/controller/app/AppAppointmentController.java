package com.situ.elder.controller.app;

import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/appointment")
public class AppAppointmentController {
    @Autowired
    private IExamAppointmentService examAppointmentService;

    /**
     * 我的预约列表
     * GET /app/appointment
     */
    @GetMapping
    public Result<List<ExamAppointmentVO>> list(@RequestHeader("Authorization") String token) {
        Long elderId = getElderIdFromToken(token);
        return Result.ok(examAppointmentService.listByElderId(elderId));
    }

    /**
     * 提交体检预约
     * POST /app/appointment
     */
    @PostMapping
    public Result add(@RequestHeader("Authorization") String token,
                      @RequestBody AppAppointmentDTO appAppointmentDTO) {
        Long elderId = getElderIdFromToken(token);
        examAppointmentService.add(appAppointmentDTO, elderId);
        return Result.ok("预约成功");
    }

    /**
     * 取消预约
     * PUT /app/appointment/1/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result cancel(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Long elderId = getElderIdFromToken(token);
        examAppointmentService.cancel(id, elderId);
        return Result.ok("取消成功");
    }

    /**
     * 查询预约的体检项目明细（含结果类型、单位、参考范围）
     */
    @GetMapping("/{id}/items")
    public Result<List<ExamAppointmentItemVO>> listItems(@PathVariable Long id){
        return Result.ok(examAppointmentService.listItems(id));
    }

    /**
     * 从token中获取老人ID
     */
    private Long getElderIdFromToken(String token) {
        // TODO: 从token中获取老人ID
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer elderId = (Integer) map.get("id");
        return elderId.longValue();
    }
}
