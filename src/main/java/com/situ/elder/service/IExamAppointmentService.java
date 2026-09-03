package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.AppAppointmentDTO;
import com.situ.elder.pojo.entity.ExamAppointment;
import com.situ.elder.pojo.entity.ExamAppointmentItem;
import com.situ.elder.pojo.query.ExamAppointmentQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.vo.ExamAppointmentItemVO;
import com.situ.elder.pojo.vo.ExamAppointmentVO;

import java.util.List;

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

    List<ExamAppointmentVO> listByElderId(Long elderId);

    void add(AppAppointmentDTO appAppointmentDTO, Long elderId);

    void cancel(Long id, Long elderId);

    /**
     * 查询预约的体检项目明细（管理后台）
     *
     * @param id 预约 id
     * @return 体检项目明细列表（含结果类型、单位、参考范围）
     */
    List<ExamAppointmentItemVO> listItems(Long id);

    /**
     * 保存体检结果（管理后台）
     * <p>
     * 逐条更新体检项目明细的结果数据，全部保存后将预约状态置为已完成（2）。
     *
     * @param id        预约 id
     * @param itemList  体检项目明细结果列表
     */
    void saveResults(Long id, List<ExamAppointmentItem> itemList);
}
