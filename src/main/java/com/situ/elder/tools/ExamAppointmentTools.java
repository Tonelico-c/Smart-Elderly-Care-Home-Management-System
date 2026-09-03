package com.situ.elder.tools;

import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.service.IExamAppointmentService;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class ExamAppointmentTools {

    private Integer elderId;
    private IExamAppointmentService examAppointmentService;
    public ExamAppointmentTools(Integer elderId, IExamAppointmentService examAppointmentService) {
        this.elderId = elderId;
        this.examAppointmentService = examAppointmentService;
    }
    @Tool(description="查询老人当前最新的体检预约信息的体检结果")
    public List<ExamAppointmentItemVO> examAppointmentItemInfo(){
        return examAppointmentService.listItems(elderId.longValue());
    }
}
