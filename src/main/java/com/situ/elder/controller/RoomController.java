package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;
import com.situ.elder.service.IRoomService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 房间表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @GetMapping
    public Result<IPage<RoomVO>> list(RoomQuery roomQuery){
        IPage<RoomVO> page = roomService.list(roomQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Room> selectById(@PathVariable Long id){
        Room room = roomService.getById(id);
        return Result.ok(room);
    }

    @PostMapping
    public Result<Room> add(@RequestBody Room room){
        roomService.save(room);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Room room){
        roomService.updateById(room);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        roomService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        roomService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
