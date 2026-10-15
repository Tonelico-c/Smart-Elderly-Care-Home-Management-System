package com.situ.elder.controller.app;

import com.situ.elder.pojo.dto.AppElderLeaveDTO;
import com.situ.elder.pojo.vo.ElderLeaveVO;
import com.situ.elder.service.IElderLeaveService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/elderleaves")
public class AppElderLeaveController {

    @Autowired
    private IElderLeaveService elderLeaveService;
    @GetMapping
    public Result<List<ElderLeaveVO>> list(@RequestHeader("Authorization") String token) {
        Long elderId = getElderIdFromToken(token);
        return Result.ok(elderLeaveService.listByElderId(elderId));
    }

    // 老人请假
    @PostMapping
    public Result add(@RequestHeader("Authorization") String token, @RequestBody AppElderLeaveDTO appElderLeaveDTO){
        Long elderId = getElderIdFromToken(token);
        elderLeaveService.add(elderId, appElderLeaveDTO);
        return Result.ok();
    }

    // 老人销假（带归属校验：只能销自己的假）
    @PostMapping("/{id}/checkout")
    public Result checkout(@RequestHeader("Authorization") String token,
                           @PathVariable Long id,
                           @RequestParam(value = "actualReturnTime", required = false) String actualReturnTime) {
        Long elderId = getElderIdFromToken(token);
        elderLeaveService.checkout(id, actualReturnTime, elderId);
        return Result.ok("销假成功");
    }

    // 老人取消当前待审批的请假记录（带归属校验：只能取消自己的记录）
    @DeleteMapping("/{id}/cancel")
    public Result cancel(@RequestHeader("Authorization") String token, @PathVariable Long id){
        Long elderId = getElderIdFromToken(token);
        elderLeaveService.cancel(id, elderId);
        return Result.ok("取消成功");
    }

    private Long getElderIdFromToken(String token) {
        // TODO: 从token中获取老人ID
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer elderId = (Integer) map.get("id");
        return elderId.longValue();
    }
}
