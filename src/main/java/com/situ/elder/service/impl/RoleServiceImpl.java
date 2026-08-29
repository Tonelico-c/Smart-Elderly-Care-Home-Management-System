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


    /**
     * 分页 + 多条件查询角色列表
     * <p>
     * 实现逻辑：
     * 1. 根据前端传入的分页参数（page、limit）构造分页对象；
     * 2. 构造 LambdaQueryWrapper，按角色名、角色编码模糊匹配，按创建时间区间过滤
     *    （条件为空时自动跳过，避免拼出无效 SQL）；
     * 3. 直接用 MyBatis-Plus 的 selectPage 完成物理分页查询。
     *
     * @param roleQuery 分页及查询条件（角色名、编码、创建时间区间）
     * @return 角色分页结果
     */
    @Override
    public IPage<Role> list(RoleQuery roleQuery) {
        // 构造分页对象：当前页码 + 每页条数
        IPage<Role> page = new Page<>(roleQuery.getPage(), roleQuery.getLimit());
        // 构造查询条件：like 只在参数非空时拼接，between 需起止时间齐全
        LambdaQueryWrapper<Role> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(roleQuery.getName()),Role::getName, roleQuery.getName())
                .like(!ObjectUtils.isEmpty(roleQuery.getCode()),Role::getCode, roleQuery.getCode())
                .between(!ObjectUtils.isEmpty(roleQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(roleQuery.getEndCreateTime()), Role::getCreateTime, roleQuery.getBeginCreateTime(), roleQuery.getEndCreateTime());
        return roleMapper.selectPage(page, lambdaQueryWrapper);
    }

    /**
     * 分页查询某角色关联的所有权限
     * <p>
     * 实现逻辑：
     * 1. 先按 roleId 查 role_permission 关联表，取出该角色绑定的全部权限 id；
     * 2. 若权限 id 集合为空，in 空集合会导致 SQL 错误，直接返回空的分页对象短路返回；
     * 3. 否则用 in (permissionIds) 对权限表做分页查询，
     *    再通过 IPage.convert 把 Permission 逐条转成 PermissionVO
     *    （convert 会保留 total、pages 等分页信息，返回新的分页对象）。
     *
     * @param pageQuery 分页参数（页码、每页条数）
     * @param roleId    角色 id
     * @return 该角色关联权限的分页结果
     */
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
                    // 同名属性自动拷贝（id、name、parentId 等）
                    BeanUtils.copyProperties(permission, permissionVO);
                    return permissionVO;
                });
    }
}
