package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 老人预约/体检记录表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
public interface IExamAppointmentService extends IService<ExamAppointment> {

    IPage<ExamAppointment> list(ExamAppointmentQuery examAppointmentQuery);
}
