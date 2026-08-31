package com.situ.elder.mapper;

import com.situ.elder.pojo.entity.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-08-27
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> selectPermissionByUserId(Long id);
}
