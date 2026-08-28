package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.PageQuery;
import com.situ.elder.pojo.query.RoleQuery;
import com.situ.elder.pojo.vo.PermissionVO;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
public interface IRoleService extends IService<Role> {

    IPage<Role> list(RoleQuery roleQuery);

    IPage<PermissionVO> selectRelatedPermission(PageQuery pageQuery, Long roleId);
}
