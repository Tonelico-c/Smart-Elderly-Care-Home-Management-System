package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareItem;
import com.situ.elder.pojo.query.CareItemQuery;
import com.situ.elder.service.ICareItemService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 护理项目表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-31
 */
@RestController
@RequestMapping("/admin/care-items")
public class CareItemController {

    @Autowired
    private ICareItemService careItemService;

    @GetMapping
    public Result<IPage<CareItem>> list(CareItemQuery careItemQuery){
        IPage<CareItem> page = careItemService.list(careItemQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<CareItem> selectById(@PathVariable Long id){
        CareItem careItem = careItemService.getById(id);
        return Result.ok(careItem);
    }

    @PostMapping
    public Result<CareItem> add(@RequestBody CareItem careItem){
        careItemService.save(careItem);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody CareItem careItem){
        careItemService.updateById(careItem);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        careItemService.removeById(id);
        return Result.ok("删除成功");
    }

    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        careItemService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }
}
