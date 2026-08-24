package com.situ.elder.service.impl;

import com.situ.elder.pojo.entity.User;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
