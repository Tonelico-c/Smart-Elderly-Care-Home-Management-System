package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.CheckInRecordMapper;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.pojo.dto.AppElderLeaveDTO;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ElderLeave;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.mapper.ElderLeaveMapper;
import com.situ.elder.pojo.query.ElderLeaveQuery;
import com.situ.elder.pojo.vo.ElderLeaveVO;
import com.situ.elder.service.IElderLeaveService;
import com.situ.elder.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 老人请假外出记录表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-03
 */
@Service
public class ElderLeaveServiceImpl extends ServiceImpl<ElderLeaveMapper, ElderLeave> implements IElderLeaveService {

    /**
     * 老人状态：入住中
     */
    private static final int ELDER_STATUS_CHECKED_IN = 4;
    /**
     * 老人状态：请假
     */
    private static final int ELDER_STATUS_ON_LEAVE = 2;

    /**
     * 请假记录状态：待审批
     */
    private static final int LEAVE_STATUS_PENDING = 0;
    /**
     * 请假记录状态：请假中
     */
    private static final int LEAVE_STATUS_ON_LEAVE = 1;
    /**
     * 请假记录状态：已驳回
     */
    private static final int LEAVE_STATUS_REJECTED = 3;
    /**
     * 请假记录状态：已销假
     */
    private static final int LEAVE_STATUS_CHECKED_OUT = 2;

    /**
     * 床位状态：请假（床位仍被占用，只是老人暂时外出，不释放）
     */
    private static final int BED_STATUS_ON_LEAVE = 4;
    /**
     * 床位状态：入住
     */
    private static final int BED_STATUS_OCCUPIED = 1;

    /**
     * 入住记录状态：入住中
     */
    private static final int CHECKIN_STATUS_CHECKED_IN = 1;
    /**
     * 入住记录状态：请假中
     */
    private static final int CHECKIN_STATUS_ON_LEAVE = 2;

    @Autowired
    private ElderLeaveMapper elderLeaveMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private CheckInRecordMapper checkInRecordMapper;
    @Autowired
    private IUserService userService;

    @Override
    public IPage<ElderLeaveVO> list(ElderLeaveQuery elderLeaveQuery) {

        IPage<ElderLeave> page = new Page<>(elderLeaveQuery.getPage(), elderLeaveQuery.getLimit());

        LambdaQueryWrapper<ElderLeave> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //前端搜索栏未选择时会传空字符串，需同时判断非空串，避免拼上 elder_id = '' 的条件
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(elderLeaveQuery.getElderId()), ElderLeave::getElderId, elderLeaveQuery.getElderId())
                .eq(elderLeaveQuery.getStatus() != null, ElderLeave::getStatus, elderLeaveQuery.getStatus());

        IPage<ElderLeave> elderLeaveIPage = elderLeaveMapper.selectPage(page, lambdaQueryWrapper);

        List<Long> elderIds = elderLeaveIPage.getRecords().stream()
                .map(ElderLeave::getElderId)
                .distinct()
                .toList();
        //当前页没有记录时集合为空，IN () 是非法SQL，直接返回空Map
        Map<Long, String> elderList = elderIds.isEmpty() ? Map.of()
                : elderMapper.selectBatchIds(elderIds)
                .stream().collect(Collectors.toMap(Elder::getId, Elder::getName));

        //批量查询审批人姓名
        Map<Long, String> approverMap = getApproverNameMap(
                elderLeaveIPage.getRecords().stream().map(ElderLeave::getApproverId).toList());

        return elderLeaveIPage.convert(elderLeave -> {
            ElderLeaveVO elderLeaveVO = new ElderLeaveVO();
            BeanUtils.copyProperties(elderLeave, elderLeaveVO);
            elderLeaveVO.setElderName(elderList.get(elderLeave.getElderId()));
            elderLeaveVO.setApproverName(approverMap.get(elderLeave.getApproverId()));
            return elderLeaveVO;
        });
    }

    /**
     * 添加请假记录（管理后台）
     * <p>
     * 只有状态为"入住中"（4）的老人才允许请假；
     * 新记录状态强制置为待审批（0），由审批流程后续流转。
     *
     * @param elderLeave 请假记录（须包含老人 id）
     * @throws ServiceException 老人不存在或状态不是入住中时抛出
     */
    @Override
    public void add(ElderLeave elderLeave) {
        if (ObjectUtils.isEmpty(elderLeave.getElderId())) {
            throw new ServiceException("请选择老人");
        }
        Elder elder = elderMapper.selectById(elderLeave.getElderId());
        if (elder == null) {
            throw new ServiceException("老人不存在");
        }
        if (elder.getStatus() == null || elder.getStatus() != ELDER_STATUS_CHECKED_IN) {
            throw new ServiceException("只有入住中的老人才可以请假");
        }

        //新记录状态固定为待审批
        elderLeave.setId(null);
        elderLeave.setStatus(LEAVE_STATUS_PENDING);
        save(elderLeave);
    }

    /**
     * 审批通过：请假记录、老人、入住记录、床位四张表在一个事务里同步
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approverId) {
        // 1. 校验记录存在且待审批,防止重复审批
        ElderLeave elderLeave = getById(id);
        if (elderLeave == null || elderLeave.getStatus() == null || elderLeave.getStatus() != LEAVE_STATUS_PENDING) {
            throw new ServiceException("该记录不存在或已审批过");
        }
        // 2. 更新请假记录:状态→请假中,写入审批人
        elderLeave.setStatus(LEAVE_STATUS_ON_LEAVE);
        elderLeave.setApproverId(approverId);
        updateById(elderLeave);
        // 3. 老人状态改为请假
        Elder elderUpdate = new Elder();
        elderUpdate.setId(elderLeave.getElderId());
        elderUpdate.setStatus(ELDER_STATUS_ON_LEAVE);
        elderMapper.updateById(elderUpdate);
        // 4. 找到该老人"入住中"的入住记录:状态改为请假中,对应床位改为请假
        //    请假不释放床位,床位只是标记为"请假"状态,销假时再改回来
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getElderId, elderLeave.getElderId())
                .eq(CheckInRecord::getStatus, CHECKIN_STATUS_CHECKED_IN)
                .last("limit 1"));
        if (checkInRecord != null) {
            CheckInRecord recordUpdate = new CheckInRecord();
            recordUpdate.setId(checkInRecord.getId());
            recordUpdate.setStatus(CHECKIN_STATUS_ON_LEAVE);
            checkInRecordMapper.updateById(recordUpdate);

            Bed bedUpdate = new Bed();
            bedUpdate.setId(checkInRecord.getBedId());
            bedUpdate.setStatus(BED_STATUS_ON_LEAVE);
            bedMapper.updateById(bedUpdate);
        }
    }

    /**
     * 审批驳回:只改请假记录本身,老人/床位/入住记录都不动
     */
    @Override
    public void reject(Long id, Long approverId, String rejectReason) {
        if (ObjectUtils.isEmpty(rejectReason)) {
            throw new ServiceException("请填写驳回理由");
        }
        ElderLeave elderLeave = getById(id);
        if (elderLeave == null || elderLeave.getStatus() == null || elderLeave.getStatus() != LEAVE_STATUS_PENDING) {
            throw new ServiceException("该记录不存在或已审批过");
        }
        elderLeave.setStatus(LEAVE_STATUS_REJECTED);
        elderLeave.setApproverId(approverId);
        elderLeave.setRejectReason(rejectReason);
        updateById(elderLeave);
    }

    /**
     * 销假：请假记录、老人、入住记录、床位四张表在一个事务里同步恢复
     * <p>
     * 仅"请假中"（1）的记录可以销假；实际返回时间未填时取当前时间。
     *
     * @param id               请假记录id
     * @param actualReturnTime 实际返回时间（yyyy-MM-dd HH:mm:ss，为空时取当前时间）
     * @throws ServiceException 记录不存在、状态不是请假中或时间格式不正确时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long id, String actualReturnTime) {
        // 1. 校验记录存在且为"请假中"，防止重复销假
        ElderLeave elderLeave = getById(id);
        if (elderLeave == null || elderLeave.getStatus() == null || elderLeave.getStatus() != LEAVE_STATUS_ON_LEAVE) {
            throw new ServiceException("该记录不存在或不是请假中，不能销假");
        }
        // 2. 解析实际返回时间,未填时取当前时间
        Date actualReturn;
        try {
            actualReturn = ObjectUtils.isEmpty(actualReturnTime)
                    ? new Date() : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(actualReturnTime);
        } catch (ParseException e) {
            throw new ServiceException("实际返回时间格式不正确");
        }
        // 3. 更新请假记录:状态→已销假,写入实际返回时间
        elderLeave.setStatus(LEAVE_STATUS_CHECKED_OUT);
        elderLeave.setActualReturnTime(actualReturn);
        updateById(elderLeave);
        // 4. 老人状态恢复为入住中
        Elder elderUpdate = new Elder();
        elderUpdate.setId(elderLeave.getElderId());
        elderUpdate.setStatus(ELDER_STATUS_CHECKED_IN);
        elderMapper.updateById(elderUpdate);
        // 5. 找到该老人"请假中"的入住记录:恢复为入住中,对应床位恢复为入住
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getElderId, elderLeave.getElderId())
                .eq(CheckInRecord::getStatus, CHECKIN_STATUS_ON_LEAVE)
                .last("limit 1"));
        if (checkInRecord != null) {
            CheckInRecord recordUpdate = new CheckInRecord();
            recordUpdate.setId(checkInRecord.getId());
            recordUpdate.setStatus(CHECKIN_STATUS_CHECKED_IN);
            checkInRecordMapper.updateById(recordUpdate);

            Bed bedUpdate = new Bed();
            bedUpdate.setId(checkInRecord.getBedId());
            bedUpdate.setStatus(BED_STATUS_OCCUPIED);
            bedMapper.updateById(bedUpdate);
        }
    }

    /**
     * 根据老人id查询请假记录（App端）
     * <p>
     * 按创建时间倒序返回，并批量补齐审批人姓名，转VO返回。
     */
    @Override
    public List<ElderLeaveVO> listByElderId(Long elderId) {
        LambdaQueryWrapper<ElderLeave> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ElderLeave::getElderId, elderId)
                        .orderByDesc(ElderLeave::getCreateTime);
        List<ElderLeave> elderLeaveList = elderLeaveMapper.selectList(lambdaQueryWrapper);

        //批量查询审批人姓名
        Map<Long, String> approverMap = getApproverNameMap(
                elderLeaveList.stream().map(ElderLeave::getApproverId).toList());

        return elderLeaveList.stream().map(elderLeave -> {
            ElderLeaveVO elderLeaveVO = new ElderLeaveVO();
            BeanUtils.copyProperties(elderLeave, elderLeaveVO);
            elderLeaveVO.setApproverName(approverMap.get(elderLeave.getApproverId()));
            return elderLeaveVO;
        }).toList();
    }

    @Override
    public void add(Long elderId, AppElderLeaveDTO appElderLeaveDTO) {

        //注意不能写成 eq(elderId).eq(status,1).or().eq(status,0)：or()在顶层，
        //生成的SQL是 (elder_id=? AND status=1) OR status=0，会导致别的老人有待审批记录时本老人也无法请假
        List<ElderLeave> elderLeaveList = elderLeaveMapper.selectList(new LambdaQueryWrapper<ElderLeave>()
                .eq(ElderLeave::getElderId, elderId)
                .in(ElderLeave::getStatus, LEAVE_STATUS_ON_LEAVE, LEAVE_STATUS_PENDING));
        if (elderLeaveList != null && !elderLeaveList.isEmpty()) {
            throw new ServiceException("当前有待审批或未销假的请假记录，请等待审批或先销假再提交新的请假记录");
        }
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new ServiceException("老人不存在");
        }

        ElderLeave elderLeave = new ElderLeave();
        elderLeave.setId(null);
        elderLeave.setElderId(elderId);
        elderLeave.setReason(appElderLeaveDTO.getReason());
        elderLeave.setDestination(appElderLeaveDTO.getDestination());
        elderLeave.setContactPhone(appElderLeaveDTO.getPhone());
        elderLeave.setBeginTime(appElderLeaveDTO.getBeginTime());
        elderLeave.setEndTime(appElderLeaveDTO.getEndTime());
        elderLeave.setStatus(LEAVE_STATUS_PENDING);
        save(elderLeave);

    }

    /**
     * 取消请假：仅能取消待审批的请假记录
     * <p>
     * 删除请假记录，不涉及老人/床位/入住记录的修改。
     *
     * @param id 请假记录id
     * @throws ServiceException 记录不存在或已审批过时抛出
     */
    @Override
    public void cancel(Long id) {
        ElderLeave elderLeave = getById(id);
        if(elderLeave == null || elderLeave.getStatus() == null || elderLeave.getStatus() != LEAVE_STATUS_PENDING){
            throw new ServiceException("该记录不存在或已审批过，不能取消");
        }
        elderLeaveMapper.deleteById(id);
    }

    /**
     * 批量查询审批人姓名
     * <p>
     * 过滤掉null（待审批的记录没有审批人）后按id批量查询，避免空集合产生 IN () 非法SQL。
     *
     * @param approverIds 审批人id集合（可能含null、可能为空）
     * @return 审批人id → 姓名映射
     */
    private Map<Long, String> getApproverNameMap(List<Long> approverIds) {
        List<Long> ids = approverIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            //注意不能返回Map.of()：不可变Map用null作key调用get会直接抛NPE
            //（未审批的请假记录approverId为null，调用方仍会拿null来查）
            return new HashMap<>();
        }
        return userService.listByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
    }
}
