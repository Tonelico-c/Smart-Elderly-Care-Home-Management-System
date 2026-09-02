package com.situ.elder.controller.app;

import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private Long getElderIdFromToken(String token) {
        // TODO: 从token中获取老人ID
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer elderId = (Integer) map.get("id");
        return elderId.longValue();
    }
}
