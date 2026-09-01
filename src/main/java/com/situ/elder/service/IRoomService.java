package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface IRoomService extends IService<Room> {

    IPage<RoomVO> list(RoomQuery roomQuery);
}
