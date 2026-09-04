package com.situ.elder.controller.app;

import com.situ.elder.pojo.vo.ElderLeaveVO;
import com.situ.elder.service.IElderLeaveService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private Long getElderIdFromToken(String token) {
        // TODO: 从token中获取老人ID
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer elderId = (Integer) map.get("id");
        return elderId.longValue();
    }
}
