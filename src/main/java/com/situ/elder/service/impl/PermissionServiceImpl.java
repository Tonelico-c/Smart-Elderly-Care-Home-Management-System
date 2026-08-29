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

import java.util.List;

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

    /*
     * 已废弃的分页查询方法（被新方案替代，仅作参考保留）：
     * 根据查询对象中的条件（父ID、权限名、权限值、创建时间区间）进行模糊/范围查询，
     * 其中 between(...) 的第三个参数是可选条件——只有当 begin 和 end 都非空时才生效。
     */
    /*@Override
    public IPage<Permission> list(PermissionQuery permissionQuery) {
        IPage<Permission> page = new Page<>(permissionQuery.getPage(), permissionQuery.getLimit());

        LambdaQueryWrapper<Permission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(permissionQuery.getParentId()), Permission::getParentId, permissionQuery.getParentId())
                        .like(!ObjectUtils.isEmpty(permissionQuery.getName()), Permission::getName, permissionQuery.getName())
                        .like(!ObjectUtils.isEmpty(permissionQuery.getPermissionValue()), Permission::getPermissionValue, permissionQuery.getPermissionValue())
                        .between(!ObjectUtils.isEmpty(permissionQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(permissionQuery.getEndCreateTime()), Permission::getCreateTime, permissionQuery.getBeginCreateTime(), permissionQuery.getEndCreateTime());

        return permissionMapper.selectPage(page, lambdaQueryWrapper);
    }*/

    /**
     * 获取所有"被作为父节点使用"的 parentId 集合。
     *
     * 功能：查出全表权限后，收集每条记录的 parentId 并去重，
     * 返回给前端后可用于判断"哪些权限节点下面挂有子节点"
     * （例如前端据此决定是否显示展开箭头、是否允许删除该节点）。
     *
     * 运作流程：
     * 1. selectList(null)：无条件查询权限表所有记录；
     * 2. stream().map(getParentId)：把每条记录映射成它的 parentId；
     * 3. distinct().toList()：去重后收集为 List；
     * 4. 封装进 PermissionVO 的 parentIds 字段返回。
     */
    /*@Override
    public PermissionVO getPermissionVO() {
        // 查出所有权限记录
        List<Permission> permissions = permissionMapper.selectList(null);
        PermissionVO permissionVO = new PermissionVO();
        // 取出每条记录的parentId，去重后收集为List
        List<Long> paretIdlist = permissions.stream().map(Permission::getParentId).distinct().toList();
        permissionVO.setParentIds(paretIdlist);
        return permissionVO;
    }*/

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
        List<PermissionVO> permissionVOTree = permissionVOList.stream()
                .filter(permissionVO -> permissionVO.getParentId() == 0)
                .map(permissionVO -> {
                    // 递归构建该根节点的children（下级节点列表）
                    permissionVO.setChildren(buildChildTree(permissionVO, permissionVOList));
                    return permissionVO;
                }).toList();

        return permissionVOTree;
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
