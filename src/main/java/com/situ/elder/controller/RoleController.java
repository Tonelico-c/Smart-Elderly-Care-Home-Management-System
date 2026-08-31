package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Role;
import com.situ.elder.pojo.query.PageQuery;
import com.situ.elder.pojo.query.RoleQuery;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.pojo.vo.UserRoleVO;
import com.situ.elder.service.IRoleService;
import com.situ.elder.service.impl.UserServiceImpl;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private IRoleService roleService;
    @Autowired
    private UserServiceImpl userServiceImpl;

    @GetMapping("/selectAssignedPermission/{roleId}")
    public Result selectAssignedPermission(@PathVariable("roleId") Long roleId) {
        Map<String, Object> map = roleService.selectAssignedPermission(roleId);
        return Result.ok(map);
    }

    @PostMapping("/assignPermission")
    public Result assignPermission(Long roleId, Long[] permissionIds) {
        roleService.assignPermission(roleId, permissionIds);
        return Result.ok("分配成功");
    }


    @GetMapping
    public Result<IPage<Role>> list(RoleQuery roleQuery){
        IPage<Role> page = roleService.list(roleQuery);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Role> selectById(@PathVariable Long id){
        return Result.ok(roleService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        roleService.removeById(id);
        return Result.ok("删除成功");
    }
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids){
        roleService.removeByIds(Arrays.asList(ids));
        return Result.ok("删除成功");
    }

    @PostMapping
    public Result<Role> add(@RequestBody Role role){
        roleService.save(role);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@RequestBody Role role){
        roleService.updateById(role);
        return Result.ok("修改成功");
    }

}

