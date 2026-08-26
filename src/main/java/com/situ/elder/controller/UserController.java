package com.situ.elder.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.UserPasswordDTO;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.service.IUserService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user){
        User dbUser = userService.getOne(new QueryWrapper<User>().eq("name",user.getName()));
        if(dbUser == null){
            return Result.error("用户名不存在");
        }
        if(!dbUser.getPassword().equals(user.getPassword())){
            return Result.error("密码错误");
        }
        // 登录成功后，判断用户是否被禁用
        if(dbUser.getStatus() == 0){
            return Result.error("该用户已被禁用");
        }

        //生成token
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbUser.getId());
        map.put("name", dbUser.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功",token);
    }


    /**
     * 分页查询用户列表
     * GET /admin/users?page=1&limit=10&name=xxx&phone=xxx
     */
    @GetMapping
    public Result<IPage<User>> list(UserQuery userQuery) {
        IPage<User> page = userService.list(userQuery);
        return Result.ok(page);
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(@RequestHeader("Authorization") String token){
        Map<String, Object> map = JwtUtil.parseToken(token);
//        String username = (String) map.get("name");
        Integer id = (Integer) map.get("id");
        User user = userService.getById(id);
        user.setPassword(null);
        return Result.ok(user);
    }
    /**
     * 根据ID查询用户
     * GET /users/1
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    /**
     * 新增用户
     * POST /users
     */
    @PostMapping
    public Result add(@RequestBody User user) {
        userService.save(user);
        return Result.ok("新增成功");
    }

    /**
     * 修改用户
     * PUT /users
     */
    @PutMapping("/{id}")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.ok("修改成功");
    }

    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestHeader("Authorization") String token,@RequestBody UserPasswordDTO userPasswordDTO){
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(userPasswordDTO.getOldPassword())) {
            return Result.error("原密码错误");
        }
        User newUser =  new User();
        newUser.setId(user.getId());
        newUser.setPassword(userPasswordDTO.getNewPassword());
        userService.updateById(newUser);
        return Result.ok("密码修改成功");
    }

    /**
     * 根据ID删除用户（逻辑删除）
     * DELETE /users/1
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除用户
     * DELETE /users
     */
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        userService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}

