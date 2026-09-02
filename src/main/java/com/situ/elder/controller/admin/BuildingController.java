package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Building;
import com.situ.elder.pojo.query.BuildingQuery;
import com.situ.elder.pojo.vo.BuildingVO;
import com.situ.elder.service.IBuildingService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 楼栋表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@RestController
@RequestMapping("/admin/buildings")
public class BuildingController {

    @Autowired
    private IBuildingService buildingService;

    @GetMapping
    public Result<IPage<BuildingVO>> list(BuildingQuery buildingQuery){
        IPage<BuildingVO> page = buildingService.list(buildingQuery);
        return Result.ok(page);
    }

    /**
     * 顶部统计数据：楼栋数量、房间总数、床位总数、入住总人数、空闲床位数量
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(){
        return Result.ok(buildingService.stats());
    }

    @GetMapping("/{id}")
    public Result<Building> selectById(@PathVariable Long id){
        Building building = buildingService.getById(id);
        return Result.ok(building);
    }

    @PostMapping
    public Result<Building> add(@RequestBody Building building){
        buildingService.save(building);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Building building){
        buildingService.updateById(building);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        buildingService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        buildingService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
