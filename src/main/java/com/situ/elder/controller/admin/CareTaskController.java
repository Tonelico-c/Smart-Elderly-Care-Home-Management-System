package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CareTaskQuery;
import com.situ.elder.service.ICareTaskService;
import com.situ.elder.service.IUserService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 护理任务与打卡记录表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/admin/care-tasks")
public class CareTaskController {

    @Autowired
    private ICareTaskService careTaskService;
    @Autowired
    private IUserService userService;

    @GetMapping
    public Result<IPage<CareTask>> list(CareTaskQuery careTaskQuery, @RequestHeader("Authorization") String token){
        Map<String, Object> map = JwtUtil.parseToken(token);
        Long userId = Long.valueOf(map.get("id").toString());
        if(userService.hasRoleCode(userId, "NURSE")){
            careTaskQuery.setUserId(userId);
        }
        IPage<CareTask> page = careTaskService.list(careTaskQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<CareTask> selectById(@PathVariable Long id){
        CareTask careTask = careTaskService.getById(id);
        return Result.ok(careTask);
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody CareTask careTask){
        careTaskService.updateById(careTask);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        careTaskService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        careTaskService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
