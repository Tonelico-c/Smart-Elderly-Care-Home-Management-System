package com.situ.elder.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Room;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;

/**
 * <p>
 * 房间表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface RoomMapper extends BaseMapper<Room> {

    IPage<RoomVO> list(IPage<RoomVO> page, RoomQuery roomQuery);
}
