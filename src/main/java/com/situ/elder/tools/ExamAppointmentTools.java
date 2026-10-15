package com.situ.elder.tools;

import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.pojo.vo.ExamAppointmentVO;
import com.situ.elder.service.IExamAppointmentService;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class ExamAppointmentTools {

    /**
     * 体检预约状态：已完成
     */
    private static final int STATUS_COMPLETED = 2;

    private Integer elderId;
    private IExamAppointmentService examAppointmentService;
    public ExamAppointmentTools(Integer elderId, IExamAppointmentService examAppointmentService) {
        this.elderId = elderId;
        this.examAppointmentService = examAppointmentService;
    }

    /**
     * 查询当前老人最近一次已完成体检的结果明细。
     * <p>
     * 这里必须分两步、并且注意参数语义：
     * 1. 先用 <b>elderId</b> 查出该老人的预约列表（listByElderId 已按预约日期、时间倒序），
     *    筛出状态为"已完成"的第一条，也就是最近一次已完成的体检；
     * 2. 再用这条预约的 <b>id</b> 去查明细——listItems 的入参是"预约ID"而不是"老人ID"。
     * <p>
     * 之前直接把 elderId 传进 listItems，查出来的是"主键恰好等于这个老人 id"的那条预约，
     * 既拿不到本人的数据，还可能读到别人的体检结果。
     */
    @Tool(description = "查询当前老人最近一次已完成体检的结果明细，包含每个体检项目的名称、结果值、单位、参考范围和是否异常。如果老人还没有已完成的体检报告，会返回空列表。")
    public List<ExamAppointmentItemVO> examAppointmentItemInfo(){
        ExamAppointmentVO latestCompleted = examAppointmentService.listByElderId(elderId.longValue())
                .stream()
                .filter(vo -> vo.getStatus() != null && vo.getStatus() == STATUS_COMPLETED)
                .findFirst()
                .orElse(null);
        if (latestCompleted == null) {
            return List.of();
        }
        return examAppointmentService.listItems(latestCompleted.getId());
    }
}
