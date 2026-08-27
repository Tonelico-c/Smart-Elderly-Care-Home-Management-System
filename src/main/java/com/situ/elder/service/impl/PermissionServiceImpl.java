package com.situ.elder.service.impl;

import com.situ.elder.pojo.entity.Permission;
import com.situ.elder.mapper.PermissionMapper;
import com.situ.elder.service.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

}
