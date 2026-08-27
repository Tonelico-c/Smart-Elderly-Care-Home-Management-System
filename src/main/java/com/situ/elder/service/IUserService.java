package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.pojo.vo.UserRoleVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
public interface IUserService extends IService<User> {

    IPage<User> list(UserQuery userQuery);

    void exportExcel(HttpServletResponse response);

    void importExcel(MultipartFile file);

    void add(User user);

    UserRoleVO selectAssignedRole(Long userId);

    void assignRole(Long userId, Long[] roleIds);
}
