package com.situ.elder.mapper;

import com.situ.elder.pojo.entity.Bed;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.situ.elder.pojo.vo.BedVO;

import java.util.List;

/**
 * <p>
 * 床位表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface BedMapper extends BaseMapper<Bed> {

    /**
     * 按房间查询床位列表（含入住老人姓名）
     */
    List<BedVO> listByRoom(Long roomId);
}
