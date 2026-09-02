package com.situ.elder.controller.admin;


import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.service.IBuildingService;
import com.situ.elder.service.ICareTaskService;
import com.situ.elder.service.IElderService;
import com.situ.elder.service.IExamAppointmentService;
import com.situ.elder.service.IUserService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 首页仪表盘 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private IElderService elderService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICareTaskService careTaskService;
    @Autowired
    private IExamAppointmentService examAppointmentService;
    @Autowired
    private IBuildingService buildingService;

    /**
     * 首页统计数据：老人数、员工数、入住概况、待办事项等
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> map = new HashMap<>();
        // 老人总数、员工总数
        map.put("elderCount", elderService.count());
        map.put("userCount", userService.count());
        // 楼栋、房间、床位、入住、空闲（复用楼栋统计）
        map.putAll(buildingService.stats());
        // 待执行护理任务数
        map.put("pendingCareTaskCount", careTaskService.lambdaQuery()
                .eq(CareTask::getStatus, 0)
                .count());
        // 待体检预约数
        map.put("pendingExamCount", examAppointmentService.lambdaQuery()
                .eq(ExamAppointment::getStatus, 0)
                .count());
        return Result.ok(map);
    }
}
