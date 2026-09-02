package com.situ.elder.controller.admin;


import com.situ.elder.pojo.entity.Permission;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.service.IPermissionService;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 权限表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
@RestController
@RequestMapping("/admin/permissions")
public class PermissionController {
    @Autowired
    private IPermissionService permissionService;

    @GetMapping("selectPermissionTree")
    public Result<List<PermissionVO>> selectPermissionTree(){
        List<PermissionVO> permissionVOList = permissionService.selectPermissionTree();
        return Result.ok(permissionVOList);
    }

    /*@GetMapping
    public Result<IPage<Permission>> list(PermissionQuery permissionQuery){
        IPage<Permission> page = permissionService.list(permissionQuery);
        return Result.ok(page);
    }*/

    @GetMapping("/{id}")
    public Result<Permission> selectById(@PathVariable Long id){
        return Result.ok(permissionService.getById(id));
    }

    /*@GetMapping("/permissionVO")
    public Result<PermissionVO> getPermissionVO(){
        PermissionVO permissionVO = permissionService.getPermissionVO();
        return Result.ok(permissionVO);
    }*/

    @PostMapping
    public Result<Permission> add(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok("添加成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        permissionService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        permissionService.removeBatchByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok("修改成功");
    }
}

