package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.query.CheckInRecordQuery;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.pojo.vo.CheckInRecordVO;
import com.situ.elder.pojo.vo.ElderVo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 入住记录表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface ICheckInRecordService extends IService<CheckInRecord> {

    /**
     * 分页联表查询入住记录
     */
    IPage<CheckInRecordVO> list(CheckInRecordQuery checkInRecordQuery);

    /**
     * 查询空闲床位（可按楼栋、房间过滤）
     */
    List<BedVO> listAvailableBeds(Long buildingId, Long roomId);

    /**
     * 查询可入住老人（状态为启用、请假、已退住）
     */
    List<ElderVo> listAvailableElder();

    /**
     * 办理入住：校验床位空闲后保存记录，同步更新床位、老人状态
     */
    void addCheckIn(CheckInRecord checkInRecord);

    /**
     * 退住：更新记录状态，同步释放床位、更新老人状态
     */
    void checkout(Long id, Date checkOutTime);
}
