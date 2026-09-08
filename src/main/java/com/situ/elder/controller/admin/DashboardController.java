package com.situ.elder.controller.admin;

import com.situ.elder.pojo.entity.*;
import com.situ.elder.service.*;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
    @Autowired
    private IElderLeaveService elderLeaveService;

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
        // 待审批请假记录
        map.put("pendingLeaveCount", elderLeaveService.lambdaQuery()
                .eq(ElderLeave::getStatus, 0)
                .count());
        return Result.ok(map);
    }

    /**
     * 老人年龄分布（首页ECharts图表）
     * <p>
     * 按生日推算每位老人的年龄，划分年龄段统计人数：
     * 60岁以下、60-69岁、70-79岁、80-89岁、90岁及以上，生日缺失的归入"未知"。
     * 返回 [{name, value}] 结构，直接供前端饼图使用。
     */
    @GetMapping("/ageDistribution")
    public Result<List<Map<String, Object>>> ageDistribution() {
        // 年龄段顺序即展示顺序
        String[] rangeNames = { "60岁以下", "60-69岁", "70-79岁", "80-89岁", "90岁及以上", "未知" };
        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (String name : rangeNames) {
            countMap.put(name, 0);
        }

        for (Elder elder : elderService.list()) {
            countMap.merge(ageRange(elder.getBirthday()), 1, Integer::sum);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (String name : rangeNames) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", countMap.get(name));
            list.add(item);
        }
        return Result.ok(list);
    }

    /**
     * 按生日判断所属年龄段
     */
    private String ageRange(Date birthday) {
        if (birthday == null) {
            return "未知";
        }
        int age = calcAge(birthday);
        if (age < 60)
            return "60岁以下";
        if (age < 70)
            return "60-69岁";
        if (age < 80)
            return "70-79岁";
        if (age < 90)
            return "80-89岁";
        return "90岁及以上";
    }

    /**
     * 按生日计算年龄（与ElderServiceImpl中口径一致：生日未到当年则减1）
     */
    private int calcAge(Date birthday) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        // 生日还没到，年龄减1
        if (now.get(Calendar.MONTH) < birth.get(Calendar.MONTH)
                || (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                        && now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age;
    }
}
