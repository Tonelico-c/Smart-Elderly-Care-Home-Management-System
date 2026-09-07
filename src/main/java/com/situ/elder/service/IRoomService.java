package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;

import java.util.List;

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

    /**
     * 新增房间，并按床位数量自动生成对应床位记录
     */
    void addRoom(Room room);

    void deleteById(Long id);

    void deleteByIds(List<Long> list);

    void update(Room room);

    List<RoomVO> listByBuildingId(Long id);

    /**
     * 根据占用床位数刷新房间状态（0空闲/1部分入住/2已满），维修中的房间不覆盖
     */
    void refreshRoomStatus(Long roomId);
}
