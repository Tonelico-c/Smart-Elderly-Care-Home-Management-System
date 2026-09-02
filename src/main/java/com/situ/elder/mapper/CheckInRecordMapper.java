package com.situ.elder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.query.CheckInRecordQuery;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.pojo.vo.CheckInRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 入住记录表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {

    /**
     * 分页联表查询入住记录
     */
    IPage<CheckInRecordVO> list(IPage<CheckInRecordVO> page, CheckInRecordQuery checkInRecordQuery);

    /**
     * TODO 查询空闲床位：bed 表中不存在 status=1（入住中）记录的床位
     * 可选参数 buildingId、roomId 用于级联筛选
     */
    List<BedVO> listAvailableBeds(@Param("buildingId") Long buildingId, @Param("roomId") Long roomId);
}
