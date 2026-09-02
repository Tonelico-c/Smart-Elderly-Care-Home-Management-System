package com.situ.elder.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.service.IElderService;
import com.situ.elder.utils.JwtUtil;
import com.situ.elder.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/elders")
public class AppElderController {
    @Autowired
    private IElderService elderService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody Elder elder){
        Elder dbUser = elderService.getOne(new QueryWrapper<Elder>().eq("name",elder.getName()));
        if(dbUser == null){
            return Result.error("用户名不存在");
        }
        if(!dbUser.getPassword().equals(elder.getPassword())){
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
}
