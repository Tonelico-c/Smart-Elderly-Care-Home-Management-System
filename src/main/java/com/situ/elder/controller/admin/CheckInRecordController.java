package com.situ.elder.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CheckInRecord;
import com.situ.elder.pojo.query.CheckInRecordQuery;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.pojo.vo.CheckInRecordVO;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.ICheckInRecordService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 入住记录表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/admin/checkin-records")
public class CheckInRecordController {

    @Autowired
    private ICheckInRecordService checkInRecordService;

    /**
     * 分页查询入住记录
     */
    @GetMapping
    public Result<IPage<CheckInRecordVO>> list(CheckInRecordQuery checkInRecordQuery) {
        IPage<CheckInRecordVO> page = checkInRecordService.list(checkInRecordQuery);
        return Result.ok(page);
    }

    /**
     * 查询空闲床位（入住分配弹窗用）
     */
    @GetMapping("/available-beds")
    public Result<List<BedVO>> listAvailableBeds(@RequestParam(required = false) Long buildingId,
                                                 @RequestParam(required = false) Long roomId) {
        List<BedVO> bedList = checkInRecordService.listAvailableBeds(buildingId, roomId);
        return Result.ok(bedList);
    }

    /**
     * 查询空闲老人（入住分配弹窗用）
     */
    @GetMapping("/available-elders")
    public Result<List<ElderVo>> listAvailableElder(){
        List<ElderVo> elderList = checkInRecordService.listAvailableElder();
        return Result.ok(elderList);
    }

    /**
     * 办理入住
     */
    @PostMapping
    public Result add(@RequestBody CheckInRecord checkInRecord) {
        checkInRecordService.addCheckIn(checkInRecord);
        return Result.ok("办理入住成功");
    }

    /**
     * 退住
     */
    @PutMapping("/{id}/checkout")
    public Result checkout(@PathVariable Long id, @RequestBody CheckInRecord checkInRecord) {
        checkInRecordService.checkout(id, checkInRecord.getCheckOutTime());
        return Result.ok("退住成功");
    }
}
