package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.PermissionQuery;
import com.situ.elder.pojo.vo.PermissionVO;

import java.util.List;

/**
 * <p>
 * 权限表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
public interface IPermissionService extends IService<Permission> {

//    IPage<Permission> list(PermissionQuery permissionQuery);

    PermissionVO getPermissionVO();

    List<PermissionVO> selectPermissionTree();
}
