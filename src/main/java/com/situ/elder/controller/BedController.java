package com.situ.elder.controller;


import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.service.IBedService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 床位表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/beds")
public class BedController {

    @Autowired
    private IBedService bedService;

    /**
     * 按房间查询床位列表（含入住老人姓名）
     */
    @GetMapping("/room/{roomId}")
    public Result<List<BedVO>> listByRoom(@PathVariable Long roomId){
        List<BedVO> list = bedService.listByRoom(roomId);
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<Bed> selectById(@PathVariable Long id){
        Bed bed = bedService.getById(id);
        return Result.ok(bed);
    }

    @PostMapping
    public Result<Bed> add(@RequestBody Bed bed){
        bedService.save(bed);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Bed bed){
        bedService.updateById(bed);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        bedService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        bedService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
