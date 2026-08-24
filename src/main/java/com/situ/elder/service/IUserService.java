package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.UserQuery;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
public interface IUserService extends IService<User> {

    IPage<User> list(UserQuery userQuery);
}
