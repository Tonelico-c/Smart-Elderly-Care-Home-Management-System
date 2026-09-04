package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ElderLeave;
import com.situ.elder.pojo.query.ElderLeaveQuery;
import com.situ.elder.pojo.vo.ElderLeaveVO;
import com.situ.elder.service.IElderLeaveService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 老人请假外出记录表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/admin/elder-leaves")
public class ElderLeaveController {

    @Autowired
    private IElderLeaveService elderLeaveService;

    @GetMapping
    public Result<IPage<ElderLeaveVO>> list(ElderLeaveQuery elderLeaveQuery) {
        IPage<ElderLeaveVO> page = elderLeaveService.list(elderLeaveQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<ElderLeave> selectById(@PathVariable Long id) {
        ElderLeave elderLeave = elderLeaveService.getById(id);
        return Result.ok(elderLeave);
    }

    @PostMapping
    public Result<ElderLeave> add(@RequestBody ElderLeave elderLeave) {
        elderLeaveService.add(elderLeave);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody ElderLeave elderLeave) {
        elderLeaveService.updateById(elderLeave);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        elderLeaveService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        elderLeaveService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }

    /**
     * 审批通过
     * POST /admin/elder-leaves/{id}/approve
     * 审批人id从token中解析(即当前登录用户),不信任前端传值
     */
    @PostMapping("/{id}/approve")
    public Result approve(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long approverId = getUserIdFromToken(token);
        elderLeaveService.approve(id, approverId);
        return Result.ok("审批通过");
    }

    /**
     * 审批驳回
     * POST /admin/elder-leaves/{id}/reject?rejectReason=xxx
     */
    @PostMapping("/{id}/reject")
    public Result reject(@PathVariable Long id, String rejectReason, @RequestHeader("Authorization") String token) {
        Long approverId = getUserIdFromToken(token);
        elderLeaveService.reject(id, approverId, rejectReason);
        return Result.ok("已驳回");
    }

    /**
     * 销假
     * POST /admin/elder-leaves/{id}/checkout?actualReturnTime=yyyy-MM-dd HH:mm:ss
     * 实际返回时间不传时后端默认取当前时间
     */
    @PostMapping("/{id}/checkout")
    public Result checkout(@PathVariable Long id,
                           @RequestParam(value = "actualReturnTime", required = false) String actualReturnTime) {
        elderLeaveService.checkout(id, actualReturnTime);
        return Result.ok("销假成功");
    }

    /**
     * 从token中解析当前登录用户的id
     */
    private Long getUserIdFromToken(String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        return id.longValue();
    }
}

