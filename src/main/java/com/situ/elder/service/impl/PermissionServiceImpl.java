package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.pojo.entity.Permission;
import com.situ.elder.mapper.PermissionMapper;
import com.situ.elder.pojo.query.PermissionQuery;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.service.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

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

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public IPage<Permission> list(PermissionQuery permissionQuery) {
        IPage<Permission> page = new Page<>(permissionQuery.getPage(), permissionQuery.getLimit());

        LambdaQueryWrapper<Permission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(permissionQuery.getParentId()), Permission::getParentId, permissionQuery.getParentId())
                        .like(!ObjectUtils.isEmpty(permissionQuery.getName()), Permission::getName, permissionQuery.getName())
                        .like(!ObjectUtils.isEmpty(permissionQuery.getPermissionValue()), Permission::getPermissionValue, permissionQuery.getPermissionValue())
                        .between(!ObjectUtils.isEmpty(permissionQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(permissionQuery.getEndCreateTime()), Permission::getCreateTime, permissionQuery.getBeginCreateTime(), permissionQuery.getEndCreateTime());

        return permissionMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public PermissionVO getPermissionVO() {
        List<Permission> permissions = permissionMapper.selectList(null);
        PermissionVO permissionVO = new PermissionVO();
        List<Long> paretIdlist = permissions.stream().map(Permission::getParentId).distinct().toList();
        permissionVO.setParentIds(paretIdlist);
        return permissionVO;
    }
}
