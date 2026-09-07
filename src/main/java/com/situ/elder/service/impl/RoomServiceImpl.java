package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.CheckInRecordMapper;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;
import com.situ.elder.service.IBedService;
import com.situ.elder.service.IRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private IBedService bedService;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private CheckInRecordMapper checkInRecordMapper;

    @Override
    public IPage<RoomVO> list(RoomQuery roomQuery) {
        IPage<RoomVO> page = new Page<>(roomQuery.getPage(), roomQuery.getLimit());
        return roomMapper.list(page, roomQuery);
    }

    /**
     * 新增房间，并按床位数量自动生成对应床位（编号如 1号床，默认空闲、费用0，可在床位管理中调整）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(Room room) {
        // 校验房型与床位数量匹配：单人间只能1个、双人间只能2个、多人间不能低于3个
        Integer roomType = room.getRoomType();
        Integer bedCount = room.getBedCount();
        if (roomType != null && bedCount != null) {
            boolean valid = (roomType == 1 && bedCount == 1)
                    || (roomType == 2 && bedCount == 2)
                    || (roomType == 3 && bedCount >= 3);
            if (!valid) {
                throw new ServiceException("床位数量与房型不符：单人间只能1个、双人间只能2个、多人间不能低于3个");
            }
        }
        // 保存房间，拿到自增 id
        this.save(room);
        // 按床位数量批量生成床位
        List<Bed> beds = new ArrayList<>();
        for (int i = 1; i <= bedCount; i++) {
            Bed bed = new Bed();
            bed.setRoomId(room.getId());
            bed.setBedNo(i + "号床");
            bed.setStatus(0);
            bed.setPrice(BigDecimal.ZERO);
            beds.add(bed);
        }
        if (!beds.isEmpty()) {
            bedService.saveBatch(beds);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        List<CheckInRecord> checkInRecordList = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .eq(CheckInRecord::getRoomId, id)
                        .in(CheckInRecord::getStatus, 1, 2));
        if (checkInRecordList != null && !checkInRecordList.isEmpty()) {
            throw new ServiceException("该楼栋有老人入住中，不允许删除");
        }
        List<Long> bedIds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, id))
                                    .stream().map(Bed::getId).toList();
        if (!bedIds.isEmpty()) {
            bedService.removeBatchByIds(bedIds);
        }
        this.removeById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> list) {
        List<CheckInRecord> checkInRecordList = checkInRecordMapper.selectList(
                new LambdaQueryWrapper<CheckInRecord>()
                        .in(CheckInRecord::getRoomId, list)
                        .in(CheckInRecord::getStatus, 1, 2));
        if (checkInRecordList != null && !checkInRecordList.isEmpty()) {
            throw new ServiceException("选中的房间中有老人入住中，不允许删除");
        }
        List<Long> bedIds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().in(Bed::getRoomId, list))
                                    .stream().map(Bed::getId).toList();
        if (!bedIds.isEmpty()) {
            bedService.removeBatchByIds(bedIds);
        }
        this.removeBatchByIds(list);
    }

    /**
     * 编辑房间：先删除原有床位，再按新的床位数量重新生成床位
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Room room) {
        // 校验房型与床位数量匹配：单人间只能1个、双人间只能2个、多人间不能低于3个
        Integer roomType = room.getRoomType();
        Integer bedCount = room.getBedCount();
        if (roomType != null && bedCount != null) {
            boolean valid = (roomType == 1 && bedCount == 1)
                    || (roomType == 2 && bedCount == 2)
                    || (roomType == 3 && bedCount >= 3);
            if (!valid) {
                throw new ServiceException("床位数量与房型不符：单人间只能1个、双人间只能2个、多人间不能低于3个");
            }
        }
        // 有老人占用（入住/请假）的床位不允许重建，否则会丢失在住关系
        List<Bed> oldBeds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, room.getId()));
        List<Long> bedIds = oldBeds.stream().filter(bed -> bed.getStatus() == 1 || bed.getStatus() == 4)
                            .map(Bed::getId).toList();
        if (!bedIds.isEmpty()) {
            throw new ServiceException("有老人占用（入住/请假）的床位不允许重建，否则会丢失在住关系");
        }
        // 先删除原有床位
        if (!oldBeds.isEmpty()) {
            bedService.removeBatchByIds(oldBeds.stream().map(Bed::getId).toList());
        }
        // 再按新的床位数量插入新床位
        if (bedCount != null && bedCount > 0) {
            List<Bed> beds = new ArrayList<>();
            for (int i = 1; i <= bedCount; i++) {
                Bed bed = new Bed();
                bed.setRoomId(room.getId());
                bed.setBedNo(i + "号床");
                bed.setStatus(0);
                bed.setPrice(BigDecimal.ZERO);
                beds.add(bed);
            }
            bedService.saveBatch(beds);
        }
        this.updateById(room);
        // 重建后床位全部空闲，同步刷新房间状态
        this.refreshRoomStatus(room.getId());
    }

    /**
     * 根据占用床位数刷新房间状态：0空闲 / 1部分入住 / 2已满
     * <p>
     * 占用床位数 = 该房间状态为入住(1)或请假(4)的床位数量（请假的床位仍被老人占用）。
     * 维修(3)为人工设置的状态，不自动覆盖。
     */
    @Override
    public void refreshRoomStatus(Long roomId) {
        if (roomId == null) {
            return;
        }
        Room room = this.getById(roomId);
        if (room == null) {
            return;
        }
        // 维修中的房间保持人工状态，不参与自动刷新
        if (room.getStatus() != null && room.getStatus() == 3) {
            return;
        }
        Long occupied = bedMapper.selectCount(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getRoomId, roomId)
                .in(Bed::getStatus, 1, 4));
        int bedCount = room.getBedCount() != null ? room.getBedCount() : 0;
        int status;
        if (occupied == 0) {
            status = 0; // 空闲
        } else if (occupied >= bedCount) {
            status = 2; // 已住满
        } else {
            status = 1; // 部分入住
        }
        Room roomUpdate = new Room();
        roomUpdate.setId(roomId);
        roomUpdate.setStatus(status);
        this.updateById(roomUpdate);
    }

    /**
     * 根据楼栋 id 获取房间列表及入住人数
     */
    @Override
    public List<RoomVO> listByBuildingId(Long id) {
        List<Room> roomList = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getBuildingId, id));
        // 一次性统计各房间的入住/请假人数（含请假中，床位仍被占用），避免逐房间查询
        Map<Long, Long> residentCountMap = new HashMap<>();
        if (!roomList.isEmpty()) {
            List<Long> roomIds = roomList.stream().map(Room::getId).toList();
            List<CheckInRecord> checkInRecordList = checkInRecordMapper.selectList(
                    new LambdaQueryWrapper<CheckInRecord>()
                            .in(CheckInRecord::getRoomId, roomIds)
                            .in(CheckInRecord::getStatus, 1, 2));
            // 逐条累计每个房间的入住/请假人数：房间已统计过则计数加一，否则从1开始
            for (CheckInRecord record : checkInRecordList) {
                Long roomId = record.getRoomId();
                if (residentCountMap.containsKey(roomId)) {
                    residentCountMap.put(roomId, residentCountMap.get(roomId) + 1);
                } else {
                    residentCountMap.put(roomId, 1L);
                }
            }
        }
        // status 已由入住/退住/换房等操作联动维护，直接拷贝即可
        return roomList.stream().map(room -> {
            RoomVO roomVo = new RoomVO();
            BeanUtils.copyProperties(room, roomVo);
            roomVo.setResidentCount(residentCountMap.getOrDefault(room.getId(), 0L).intValue());
            return roomVo;
        }).toList();
    }

}
