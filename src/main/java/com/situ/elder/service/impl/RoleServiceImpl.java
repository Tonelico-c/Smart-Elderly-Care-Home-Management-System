package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.PermissionMapper;
import com.situ.elder.mapper.RolePermissionMapper;
import com.situ.elder.pojo.entity.Role;
import com.situ.elder.mapper.RoleMapper;
import com.situ.elder.pojo.entity.RolePermission;
import com.situ.elder.pojo.query.RoleQuery;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.service.IPermissionService;
import com.situ.elder.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private IPermissionService permissionService;


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


    @Override
    public Map<String, Object> selectAssignedPermission(Long roleId) {
        // 所有权限的树形结构
        List<PermissionVO> permissionVOList = permissionService.selectPermissionTree();
        // 角色已有的权限ID列表
        LambdaQueryWrapper<RolePermission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissionList = rolePermissionMapper.selectList(lambdaQueryWrapper);
        List<Long> assignedPermissionIds = rolePermissionList.stream().map(RolePermission::getPermissionId).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("permissionVOList", permissionVOList);
        map.put("assignedPermissionIds", assignedPermissionIds);
        return map;
    }

    @Override
    public void assignPermission(Long roleId, Long[] permissionIds) {
        // 先删除再添加
        // 先删除角色现有的权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        // 再添加角色新的权限
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
