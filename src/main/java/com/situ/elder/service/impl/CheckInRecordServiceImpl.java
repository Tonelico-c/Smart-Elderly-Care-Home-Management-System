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

    /**
     * 分页查询入住记录列表
     * <p>
     * 根据查询条件（老人姓名、楼栋、房间、入住/退住状态等）分页查询入住记录，
     * 联表查出老人姓名、床位编号、楼栋名称等展示信息。
     *
     * @param checkInRecordQuery 列表查询条件（含分页参数）
     * @return 分页后的入住记录视图对象列表
     */
    @Override
    public IPage<CheckInRecordVO> list(CheckInRecordQuery checkInRecordQuery) {
        IPage<CheckInRecordVO> page = new Page<>(checkInRecordQuery.getPage(), checkInRecordQuery.getLimit());
        return checkInRecordMapper.list(page, checkInRecordQuery);
    }

    /**
     * 查询可分配（空闲）的床位列表
     * <p>
     * 办理入住时供前端选择床位使用。可按楼栋、房间进一步筛选，
     * 只返回状态为空闲且没有被在住记录占用的床位，并附带楼栋/房间名称。
     *
     * @param buildingId 楼栋 id（可选，为 null 时不按楼栋过滤）
     * @param roomId     房间 id（可选，为 null 时不按房间过滤）
     * @return 可分配床位视图对象列表
     */
    @Override
    public List<BedVO> listAvailableBeds(Long buildingId, Long roomId) {
        return checkInRecordMapper.listAvailableBeds(buildingId, roomId);
    }

    /**
     * 查询可办理入住的老人列表
     * <p>
     * 只返回可以办理入住的老人：状态为启用（1）、请假（2）、已退住（5）的老人。
     * 已是"入住中（4）"的老人不可重复办理，禁用（0）和退住中（3）的老人也不可选。
     * 查询结果转换为 ElderVo 返回给前端展示。
     *
     * @return 可办理入住的老人视图对象列表
     */
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

    /**
     * 办理老人入住
     * <p>
     * 整体流程在一个事务中执行，任一步骤失败（抛出异常）则全部回滚：
     * <ol>
     *     <li>校验床位存在且状态为空闲，排除维修/停用/已占用的情况；</li>
     *     <li>校验该床位没有其他"在住"状态的记录，防止并发下重复分配同一床位；</li>
     *     <li>校验该老人没有其他"在住"状态的记录，防止重复办理入住；</li>
     *     <li>根据床位反查房间，补全记录上的房间 id 和楼栋 id；</li>
     *     <li>保存入住记录（状态置为在住，入住时间为空则默认当前时间）；</li>
     *     <li>将床位状态同步更新为"入住"；</li>
     *     <li>将老人状态同步更新为"入住中"。</li>
     * </ol>
     *
     * @param checkInRecord 前端提交的入住记录（须包含老人 id 和床位 id）
     * @throws ServiceException 床位不存在、床位不可分配、床位已被占用或老人已入住时抛出
     */
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

    /**
     * 办理老人退住
     * <p>
     * 整体流程在一个事务中执行，任一步骤失败则全部回滚：
     * <ol>
     *     <li>校验入住记录存在且状态为"在住"，已退住的记录不允许重复退住；</li>
     *     <li>将记录状态改为已退住（0），退住时间为空则默认当前时间；</li>
     *     <li>将床位状态释放为"空闲"，便于后续重新分配；</li>
     *     <li>将老人状态同步更新为"已退住"。</li>
     * </ol>
     *
     * @param id           入住记录 id
     * @param checkOutTime 退住时间（为 null 时取当前时间）
     * @throws ServiceException 记录不存在或已退住时抛出
     */
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(Long id,CheckInRecord checkInRecord) {
        Bed bed = bedMapper.selectById(checkInRecord.getBedId());
        // 1. 校验记录存在且入住中
        CheckInRecord record = checkInRecordMapper.selectById(id);
        if (record == null || record.getStatus() == null || record.getStatus() != 1) {
            throw new ServiceException("该记录不存在或已退住");
        }
        // 2. 更新记录的房间和床位信息
        record.setStatus(0);
        record.setCheckOutTime(checkInRecord.getCheckOutTime() != null ? checkInRecord.getCheckOutTime() : new Date());
        this.updateById(record);
        // 3. 释放床位
        Bed bedUpdate = new Bed();
        bedUpdate.setId(record.getBedId());
        bedUpdate.setStatus(BED_STATUS_FREE);
        bedMapper.updateById(bedUpdate);
        // 4. 根据床位补全房间、楼栋信息
        Room room = roomMapper.selectById(bed.getRoomId());
        if (room != null) {
            checkInRecord.setRoomId(room.getId());
            checkInRecord.setBuildingId(room.getBuildingId());
        }
        // 新记录主键由数据库自增生成，忽略前端误传的id，防止主键冲突；换房不改变老人，以原记录为准
        checkInRecord.setId(null);
        checkInRecord.setElderId(record.getElderId());
        checkInRecord.setStatus(1);
        if (checkInRecord.getCheckInTime() == null) {
            checkInRecord.setCheckInTime(new Date());
        }
        this.save(checkInRecord);
        // 5. 同步更新床位状态为入住
        bedUpdate.setId(bed.getId());
        bedUpdate.setStatus(BED_STATUS_OCCUPIED);
        bedMapper.updateById(bedUpdate);
    }
}
