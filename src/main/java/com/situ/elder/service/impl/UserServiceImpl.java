package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public IPage<User> list(UserQuery userQuery) {
        IPage<User> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        /*QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(!ObjectUtils.isEmpty(userQuery.getName())){
            queryWrapper.like("name", userQuery.getName());
        }
        if(!ObjectUtils.isEmpty(userQuery.getEmail())) {
            queryWrapper.like("email", userQuery.getEmail());
        }*/

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), User::getName, userQuery.getName())
                .like(!ObjectUtils.isEmpty(userQuery.getEmail()), User::getEmail, userQuery.getEmail())
                .between(!ObjectUtils.isEmpty(userQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(userQuery.getEndCreateTime()), User::getCreateTime, userQuery.getBeginCreateTime(), userQuery.getEndCreateTime())
                .orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
}
