package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Tag;
import com.situ.elder.pojo.query.TagQuery;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.ITagService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * <p>
 * 标签表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-26
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private ITagService tagService;

    @GetMapping
    public Result<IPage<Tag>> list(TagQuery tagQuery){
        IPage<Tag> page = tagService.list(tagQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Tag> selectById(@PathVariable Long id){
        Tag tag = tagService.getById(id);
        return Result.ok(tag);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        tagService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        tagService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }

    @PostMapping
    public Result<Tag> add(@RequestBody Tag tag){
        tagService.save(tag);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Tag tag){
        tagService.updateById(tag);
        return Result.ok("修改成功");
    }
}

