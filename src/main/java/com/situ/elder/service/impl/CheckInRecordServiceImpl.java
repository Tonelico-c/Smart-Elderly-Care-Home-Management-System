package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.CheckInRecordMapper;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.CheckInRecordQuery;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.pojo.vo.CheckInRecordVO;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.ICheckInRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 入住记录表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class CheckInRecordServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord> implements ICheckInRecordService {

    /**
     * 床位状态（0：空闲，1：入住，2：维修，3：停用）
     */
    private static final int BED_STATUS_FREE = 0;
    private static final int BED_STATUS_OCCUPIED = 1;

    /**
     * 老人状态（0：禁用，1：启用，2：请假，3：退住中，4：入住中，5：已退住）
     */
    private static final int ELDER_STATUS_CHECKED_IN = 4;
    private static final int ELDER_STATUS_CHECKED_OUT = 5;

    @Autowired
    private CheckInRecordMapper checkInRecordMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private RoomMapper roomMapper;

    @Override
    public IPage<CheckInRecordVO> list(CheckInRecordQuery checkInRecordQuery) {
        IPage<CheckInRecordVO> page = new Page<>(checkInRecordQuery.getPage(), checkInRecordQuery.getLimit());
        return checkInRecordMapper.list(page, checkInRecordQuery);
    }

    @Override
    public List<BedVO> listAvailableBeds(Long buildingId, Long roomId) {
        return checkInRecordMapper.listAvailableBeds(buildingId, roomId);
    }

    @Override
    public List<ElderVo> listAvailableElder() {
        LambdaQueryWrapper<Elder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Elder::getStatus, Arrays.asList(1, 2, 5));
        List<Elder> elders = elderMapper.selectList(lambdaQueryWrapper);
        return elders.stream().map(elder -> {
            ElderVo elderVo = new ElderVo();
            BeanUtils.copyProperties(elder, elderVo);
            return elderVo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCheckIn(CheckInRecord checkInRecord) {
        // 1. 校验床位存在且可分配
        Bed bed = bedMapper.selectById(checkInRecord.getBedId());
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        if (bed.getStatus() != null && bed.getStatus() != BED_STATUS_FREE) {
            throw new ServiceException("该床位当前不可分配（维修/停用/已占用）");
        }
        // 2. 校验床位没有在住记录，防止重复分配
        Long bedCount = checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getBedId, bed.getId())
                .eq(CheckInRecord::getStatus, 1));
        if (bedCount > 0) {
            throw new ServiceException("该床位已有老人入住，请重新选择");
        }
        // 3. 校验老人没有在住记录
        Long elderCount = checkInRecordMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getElderId, checkInRecord.getElderId())
                .eq(CheckInRecord::getStatus, 1));
        if (elderCount > 0) {
            throw new ServiceException("该老人已办理入住，无需重复办理");
        }
        // 4. 根据床位补全房间、楼栋信息
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            checkInRecord.setRoomId(room.getId());
            checkInRecord.setBuildingId(room.getBuildingId());
        }
        checkInRecord.setStatus(1);
        if (checkInRecord.getCheckInTime() == null) {
            checkInRecord.setCheckInTime(new Date());
        }
        this.save(checkInRecord);
        // 5. 同步更新床位状态为入住
        Bed bedUpdate = new Bed();
        bedUpdate.setId(bed.getId());
        bedUpdate.setStatus(BED_STATUS_OCCUPIED);
        bedMapper.updateById(bedUpdate);
        // 6. 同步更新老人状态为入住中
        Elder elderUpdate = new Elder();
        elderUpdate.setId(checkInRecord.getElderId());
        elderUpdate.setStatus(ELDER_STATUS_CHECKED_IN);
        elderMapper.updateById(elderUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long id, Date checkOutTime) {
        // 1. 校验记录存在且入住中
        CheckInRecord record = this.getById(id);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            throw new ServiceException("该记录不存在或已退住");
        }
        // 2. 更新记录状态为已退住
        record.setStatus(0);
        record.setCheckOutTime(checkOutTime != null ? checkOutTime : new Date());
        this.updateById(record);
        // 3. 释放床位
        Bed bedUpdate = new Bed();
        bedUpdate.setId(record.getBedId());
        bedUpdate.setStatus(BED_STATUS_FREE);
        bedMapper.updateById(bedUpdate);
        // 4. 老人状态改为已退住
        Elder elderUpdate = new Elder();
        elderUpdate.setId(record.getElderId());
        elderUpdate.setStatus(ELDER_STATUS_CHECKED_OUT);
        elderMapper.updateById(elderUpdate);
    }
}
