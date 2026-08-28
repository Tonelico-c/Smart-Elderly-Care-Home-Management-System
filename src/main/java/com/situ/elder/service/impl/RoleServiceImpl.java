package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.PermissionMapper;
import com.situ.elder.mapper.RolePermissionMapper;
import com.situ.elder.pojo.entity.Permission;
import com.situ.elder.pojo.entity.Role;
import com.situ.elder.mapper.RoleMapper;
import com.situ.elder.pojo.entity.RolePermission;
import com.situ.elder.pojo.query.PageQuery;
import com.situ.elder.pojo.query.RoleQuery;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.service.IRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;


    @Override
    public IPage<Role> list(RoleQuery roleQuery) {
        IPage<Role> page = new Page<>(roleQuery.getPage(), roleQuery.getLimit());
        LambdaQueryWrapper<Role> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(roleQuery.getName()),Role::getName, roleQuery.getName())
                .like(!ObjectUtils.isEmpty(roleQuery.getCode()),Role::getCode, roleQuery.getCode())
                .between(!ObjectUtils.isEmpty(roleQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(roleQuery.getEndCreateTime()), Role::getCreateTime, roleQuery.getBeginCreateTime(), roleQuery.getEndCreateTime());
        return roleMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public IPage<PermissionVO> selectRelatedPermission(PageQuery pageQuery, Long roleId) {
        // 根据角色ID查询相关联的权限ID列表
        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId))
                .stream().map(RolePermission::getPermissionId).toList();
        // 该角色没有关联权限时，in条件的集合为空会导致SQL错误，直接返回空分页
        if (ObjectUtils.isEmpty(permissionIds)) {
            return new Page<>(pageQuery.getPage(), pageQuery.getLimit());
        }
        // 根据权限ID列表分页查询权限列表
        return permissionMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getLimit()),
                        new LambdaQueryWrapper<Permission>().in(Permission::getId, permissionIds))
                // .convert(...) 是 IPage 自带的方法，它会把 records 转换后放回一个新的 Page，同时保留 total、pages、current、size，所以返回值还是 IPage<PermissionVO>，直接就能 return。
                .convert(permission -> {
                    PermissionVO permissionVO = new PermissionVO();
                    BeanUtils.copyProperties(permission, permissionVO);
                    return permissionVO;
                });
    }
}
