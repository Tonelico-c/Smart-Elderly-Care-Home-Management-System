package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.AppElderLeaveDTO;
import com.situ.elder.pojo.entity.ElderLeave;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.ElderLeaveQuery;
import com.situ.elder.pojo.vo.ElderLeaveVO;
import com.situ.elder.exception.ServiceException;

import java.util.List;

/**
 * <p>
 * 老人请假外出记录表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-03
 */
public interface IElderLeaveService extends IService<ElderLeave> {

    IPage<ElderLeaveVO> list(ElderLeaveQuery elderLeaveQuery);

    /**
     * 添加请假记录（管理后台）
     * <p>
     * 校验老人存在且状态为"入住中"（4），否则不允许请假；
     * 新记录状态强制置为待审批（0），不信任前端传入的状态。
     *
     * @param elderLeave 请假记录（须包含老人 id）
     * @throws ServiceException 老人不存在或状态不是入住中时抛出
     */
    void add(ElderLeave elderLeave);

    /**
     * 审批通过（事务内同步多张表）
     * <p>
     * 记录状态 0(待审批) → 1(请假中)，写入审批人id；
     * 老人状态 4(入住中) → 2(请假)；
     * 该老人当前"入住中"的入住记录状态 → 2(请假中)；
     * 对应床位状态 → 4(请假)。
     *
     * @param id         请假记录id
     * @param approverId 审批人（当前登录用户）id
     * @throws ServiceException 记录不存在或状态不是待审批时抛出
     */
    void approve(Long id, Long approverId);

    /**
     * 审批驳回（仅改记录本身，不影响老人/床位/入住记录）
     * <p>
     * 记录状态 0(待审批) → 3(已驳回)，写入审批人id和驳回理由。
     *
     * @param id           请假记录id
     * @param approverId   审批人（当前登录用户）id
     * @param rejectReason 驳回理由（必填）
     * @throws ServiceException 记录不存在、状态不是待审批或驳回理由为空时抛出
     */
    void reject(Long id, Long approverId, String rejectReason);

    /**
     * 销假（事务内同步多张表）
     * <p>
     * 仅"请假中"（1）的记录可以销假：记录状态 → 2(已销假)，写入实际返回时间
     * （为空时取当前时间）；老人状态恢复为 4(入住中)；
     * 该老人当前"请假中"的入住记录恢复为 1(入住中)，对应床位恢复为 1(入住)。
     *
     * @param id                请假记录id
     * @param actualReturnTime  实际返回时间（yyyy-MM-dd HH:mm:ss，为空时取当前时间）
     * @throws ServiceException 记录不存在或状态不是请假中时抛出
     */
    void checkout(Long id, String actualReturnTime);

    /**
     * 销假（App 端专用，带归属校验）
     * <p>
     * 与管理后台的区别：这里必须校验这条请假记录属于当前登录老人，
     * 否则老人拿着自己的合法 token、改一下路径上的 id 就能销掉别人的假。
     *
     * @param id               请假记录id
     * @param actualReturnTime 实际返回时间（为空时取当前时间）
     * @param elderId          当前登录老人id（来自 token，用于归属校验）
     * @throws ServiceException 记录不存在、状态不是请假中，或不属于该老人时抛出
     */
    void checkout(Long id, String actualReturnTime, Long elderId);

    List<ElderLeaveVO> listByElderId(Long elderId);

    void add(Long elderId, AppElderLeaveDTO appElderLeaveDTO);

    void cancel(Long id);

    /**
     * 取消请假（App 端专用，带归属校验）
     * <p>
     * 仅能取消待审批的记录，且必须是当前登录老人自己的记录。
     *
     * @param id      请假记录id
     * @param elderId 当前登录老人id（来自 token，用于归属校验）
     * @throws ServiceException 记录不存在、已审批过，或不属于该老人时抛出
     */
    void cancel(Long id, Long elderId);
}
