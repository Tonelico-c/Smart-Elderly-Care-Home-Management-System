package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.query.ElderQuery;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.IElderService;
import com.situ.elder.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 老人表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-25
 */
@RestController
@RequestMapping("/elders")
public class ElderController {

    @Autowired
    private IElderService elderService;

    @GetMapping
    public Result<IPage<ElderVo>> list(ElderQuery elderQuery){
        IPage<ElderVo> page = elderService.list(elderQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Elder> get(@PathVariable Integer id){
        Elder elder = elderService.getById(id);
        return Result.ok(elder);
    }

    @GetMapping("/selectAssignedTag/{elderId}")
    public Result<Map<String, Object>> selectAssignedTag(@PathVariable Long elderId){
        Map<String, Object> map = elderService.selectAssignedTag(elderId);
        return Result.ok(map);
    }

    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response){
        elderService.exportExcel(response);
    }

    @PostMapping
    public Result<Elder> add(@RequestBody Elder elder){
        elderService.save(elder);
        return Result.ok("添加成功");
    }

    @PostMapping("/assignTag")
    public Result assignTag(Long elderId, Long[] tagIds){
        elderService.assignTag(elderId, tagIds);
        return Result.ok("分配标签成功");
    }

    @PutMapping("/{id}")
    public Result<Elder> update(@RequestBody Elder elder){
        elderService.updateById(elder);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        elderService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result batchDelete(@RequestBody Long[] ids){
        elderService.removeBatchByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}

