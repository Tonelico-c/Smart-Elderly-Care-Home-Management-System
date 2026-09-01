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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 权限表 服务实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，自动拥有基础的增删改查能力；
 * 同时实现 IPermissionService 接口，提供权限业务相关的自定义方法。
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    // 注入权限表的 Mapper，用于直接执行数据库查询（selectList、selectPage 等）
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 查询所有权限并组装成树形结构（用于前端渲染菜单/权限树）。
     *
     * 运作流程：
     * 1. 按 sort 字段升序查出所有权限（保证同级节点的显示顺序正确）；
     * 2. 把实体 Permission 逐个拷贝属性为 PermissionVO（BeanUtils.copyProperties
     *    按同名字段复制，隔离实体与视图对象）；
     * 3. 从 VO 集合中过滤出 parentId == 0 的一级（根）节点；
     * 4. 对每个根节点递归调用 buildChildTree 填充 children，
     *   最终得到"根节点 -> 子节点 -> 孙节点 ..."的嵌套树。
     */
    @Override
    public List<PermissionVO> selectPermissionTree() {
        LambdaQueryWrapper<Permission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 按sort升序排序，查出所有权限
        lambdaQueryWrapper.orderByAsc(Permission::getSort);
        // 执行查询，得到排序后的全量权限列表
        List<Permission> permissionList = permissionMapper.selectList(lambdaQueryWrapper);
        // 转为PermissionVO集合
        List<PermissionVO> permissionVOList = permissionList.stream()
                .map(permission -> {
                    PermissionVO permissionVO = new PermissionVO();
                    BeanUtils.copyProperties(permission, permissionVO);
                    return permissionVO;
                }).toList();

        // 构建一级父节点集合：parentId == 0 即为根节点
        List<PermissionVO> permissionVOTree = buildTree(permissionVOList);

        return permissionVOTree;
    }

    /**
     * 将"扁平的权限 VO 列表"组装成树形结构（构建一级/根节点层级）。
     *
     * 约定：parentId == 0 表示该节点是一级（根）节点，没有更上层的父节点。
     *
     * 实现逻辑：
     * 1. filter 筛出所有 parentId == 0 的根节点，它们就是树的第一层；
     * 2. 对每个根节点调用 buildChildTree 递归填充 children 字段，
     *    由后者在同一份扁平列表里逐层向下找出子孙节点；
     * 3. 收集为 List 返回，即"根节点列表，每个根节点内嵌整棵子树"。
     *
     * 该方法同时被两处复用：
     * - selectPermissionTree：全量权限树（角色分配权限时的树形展示）；
     * - selectPermissionByUserId：某用户被授权的权限树（登录后动态生成菜单）。
     *
     * @param permissionVOList 扁平的权限 VO 列表（不要求排序，但排序后同级展示顺序更稳定）
     * @return 树形结构的根节点列表
     */
    public List<PermissionVO> buildTree(List<PermissionVO> permissionVOList) {
        return permissionVOList.stream()
                .filter(permissionVO -> permissionVO.getParentId() == 0)
                .map(permissionVO -> {
                    // 递归构建该根节点的children（下级节点列表）
                    permissionVO.setChildren(buildChildTree(permissionVO, permissionVOList));
                    return permissionVO;
                }).toList();
    }

    /**
     * 根据用户 ID 查询其拥有的权限，并拆成"路由（菜单）+ 按钮"两部分返回。
     *
     * 背景：permission 表用 type 字段区分权限类型——
     * type == 0/1 表示目录/菜单权限（对应前端页面），
     * type == 2 表示按钮级权限（对应页面内的操作，如"删除用户"按钮）。
     *
     * 实现逻辑：
     * 1. 调用 Mapper 的 selectPermissionByUserId，通过 user -> user_role -> role -> role_permission
     *    多表联查拿到该用户被授权的全量权限；
     * 2. 遍历权限列表按 type 分流：
     *    - 按钮权限：只收集其权限标识 permissionValue（如 "user:delete"），
     *      前端拿到后用自定义指令控制按钮显隐；
     *    - 菜单权限：拷贝为 PermissionVO；
     * 3. 菜单 VO 列表再经 buildTree 组装成树形（登录后动态生成侧边栏菜单）；
     * 4. 用 Map.of 打包成 { routerList: 菜单树, btnList: 按钮权限标识列表 } 一次性返回。
     *
     * @param id 用户 ID
     * @return routerList 为该用户的菜单权限树，btnList 为其按钮权限标识集合
     */
    @Override
    public Map<String, Object> selectPermissionByUserId(Long id) {
        // 根据用户ID查询权限
        List<Permission> permissionList = permissionMapper.selectPermissionByUserId(id);
        List<String> btnList = new ArrayList<>();
        List<PermissionVO> permissionVOList = new ArrayList<>();
        permissionList.forEach(permission -> {
            if (permission.getType() == 2) {
                // 如果是按钮权限，添加到按钮列表
                btnList.add(permission.getPermissionValue());
            }else{
                PermissionVO permissionVO = new PermissionVO();
                BeanUtils.copyProperties(permission, permissionVO);
                permissionVOList.add(permissionVO);
            }
        });
        return Map.of("routerList", buildTree(permissionVOList), "btnList", btnList);
    }

    /**
     * 递归构建子节点树（selectPermissionTree 的辅助方法）。
     *
     * 功能：从全量 VO 列表中找出 parentId 等于指定父节点 id 的直接子节点，
     * 并对每个子节点递归调用自身继续填充其 children，直到没有更深的节点为止。
     *
     * 运作流程：
     * 1. filter：筛出"父ID == 当前节点ID"的所有直接子节点；
     * 2. map：对每个子节点递归调用 buildChildTree 求出它自己的子节点列表；
     * 3. setChildren 挂到子节点上，最终返回这层子节点的列表。
     */
    public List<PermissionVO> buildChildTree(PermissionVO parentPermissionVO, List<PermissionVO> permissionVOList){
        return permissionVOList.stream().filter(permissionVO -> permissionVO.getParentId().equals(parentPermissionVO.getId()))
                .map(permissionVO -> {
                    permissionVO.setChildren(buildChildTree(permissionVO, permissionVOList)); // 构建子节点的children
                    return permissionVO;
                }).toList();
    }
}
