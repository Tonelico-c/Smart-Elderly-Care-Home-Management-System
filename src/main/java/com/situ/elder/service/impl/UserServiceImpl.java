package com.situ.elder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.excelListener.UserExcelListener;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.RoleMapper;
import com.situ.elder.mapper.UserRoleMapper;
import com.situ.elder.pojo.entity.Role;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.entity.UserRole;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.pojo.vo.UserExcelVO;
import com.situ.elder.pojo.vo.UserRoleVO;
import com.situ.elder.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 分页 + 多条件查询用户列表
     * <p>
     * 实现逻辑：
     * 1. 根据前端传入的分页参数（page、limit）构造分页对象；
     * 2. 构造 LambdaQueryWrapper，按姓名、邮箱模糊匹配，按创建时间区间过滤
     *    （条件为空时自动跳过），并按创建时间倒序排列（新用户在前）；
     * 3. 直接用 MyBatis-Plus 的 selectPage 完成物理分页查询。
     * <p>
     * 中间注释掉的代码是旧版 QueryWrapper 字符串列名写法，
     * 已改为 Lambda 方式（编译期检查列名，重构更安全）。
     *
     * @param userQuery 分页及查询条件（姓名、邮箱、创建时间区间）
     * @return 用户分页结果
     */
    @Override
    public IPage<User> list(UserQuery userQuery) {
        // 构造分页对象：当前页码 + 每页条数
        IPage<User> page = new Page<>(userQuery.getPage(), userQuery.getLimit());

        // 构造查询条件：like 只在参数非空时拼接，between 需起止时间齐全，最后按创建时间倒序
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), User::getName, userQuery.getName())
                .like(!ObjectUtils.isEmpty(userQuery.getEmail()), User::getEmail, userQuery.getEmail())
                .between(!ObjectUtils.isEmpty(userQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(userQuery.getEndCreateTime()), User::getCreateTime, userQuery.getBeginCreateTime(), userQuery.getEndCreateTime())
                .orderByDesc(User::getCreateTime);
        // 按角色编码过滤（如 NURSE 护理人员）：先查该角色下的用户ID，再限定用户范围
        if (!ObjectUtils.isEmpty(userQuery.getRoleCode())) {
            Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", userQuery.getRoleCode()));
            if (role == null) {
                return page;
            }
            List<Long> userIds = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("role_id", role.getId()))
                    .stream().map(UserRole::getUserId).toList();
            if (userIds.isEmpty()) {
                return page;
            }
            wrapper.in(User::getId, userIds);
        }
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 导出全部用户信息为 Excel 并写入响应流
     * <p>
     * 实现逻辑：
     * 1. 无条件查出所有用户；
     * 2. User 转 UserExcelVO（同名属性自动拷贝，字段上的 @ExcelProperty 决定列名）；
     * 3. 调用工具类通过 EasyExcel 写出，文件名为"用户信息表"。
     *
     * @param response HTTP 响应，Excel 文件直接写回给浏览器下载
     */
    @Override
    public void exportExcel(HttpServletResponse response) {
        // 查询全部用户（selectList 传 null 表示无条件查全表）
        List<User> userList = userMapper.selectList(null);
        // 实体转 Excel 导出专用 VO
        List<UserExcelVO> userExcelVOList = userList.stream().map(user -> {
            UserExcelVO userExcelVO = new UserExcelVO();
            BeanUtils.copyProperties(user, userExcelVO);
            return userExcelVO;
        }).toList();
        // 导出excel
        ExcelUtil.exportExcel(response, userExcelVOList, UserExcelVO.class, "用户信息表");
    }

    /**
     * 导入 Excel 批量新增用户
     * <p>
     * 实现逻辑：EasyExcel 按行读取上传文件，每行的解析和入库逻辑
     * 封装在 UserExcelListener 监听器中（读一行回调一次），
     * 这里只负责发起读取并处理 IO 异常。
     *
     * @param file 前端上传的 Excel 文件
     */
    @Override
    public void importExcel(MultipartFile file) {
        try {
            // 指定行对象类型和监听器，sheet() 不传参表示读第一个 sheet
            EasyExcel.read(file.getInputStream(), UserExcelVO.class, new UserExcelListener(userMapper)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增用户
     * <p>
     * 实现逻辑：先按用户名查询，若已存在则抛出 ServiceException
     * 由全局异常处理器转成友好提示返回前端；不存在才执行插入。
     *
     * @param user 待新增的用户信息
     * @throws ServiceException 用户名已存在时抛出
     */
    @Override
    public void add(User user) {

        log.info("添加用户: {}", user);
        // 按用户名查库，做唯一性校验
        User dbUser = userMapper.selectOne(new QueryWrapper<User>().eq("name", user.getName()));
        if(dbUser != null){
            log.error("添加失败，用户名已存在");
            throw new ServiceException("用户名已存在");
        }
        log.info("用户添加成功: {}", user);
        userMapper.insert(user);
    }

    /**
     * 查询指定用户的"分配角色"回显数据
     * <p>
     * 实现逻辑：
     * 1. 查出系统中所有角色（供前端渲染全量角色复选框）；
     * 2. 按 userId 查 user_role 关联表，取出该用户已绑定的角色 id 集合；
     * 3. 两者封装进 UserRoleVO：roleList = 全部角色，assignedRoleIdList = 已选中项。
     *
     * @param userId 用户 id
     * @return 全部角色列表 + 该用户已分配的角色 id 列表
     */
    @Override
    public UserRoleVO selectAssignedRole(Long userId) {
        // 查询所有角色
        List<Role> roleList = roleMapper.selectList(null);
        // 查询已分配的角色Id
        List<Long> assignedRoleIdList = userRoleMapper.selectList(new QueryWrapper<UserRole>().eq("user_id", userId))
                .stream().map(UserRole::getRoleId).toList();
        UserRoleVO userRoleVO = new UserRoleVO();
        userRoleVO.setRoleList(roleList);
        userRoleVO.setAssignedRoleIdList(assignedRoleIdList);
        return userRoleVO;
    }

    /**
     * 重新分配用户的角色（全量覆盖式）
     * <p>
     * 实现逻辑：先删除该用户在 user_role 表中的所有旧关联，
     * 再把前端传来的新角色 id 逐条插入，等价于"先删后增"的全量更新。
     * 注：未加事务注解，中途失败可能产生部分插入，必要时可加 @Transactional。
     *
     * @param userId  用户 id
     * @param roleIds 新的角色 id 数组
     */
    @Override
    public void assignRole(Long userId, Long[] roleIds) {
        // 删除原有角色
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", userId));
        // 添加新角色, 遍历roleIds数组, 将每个roleId与userId组合成UserRole对象并插入数据库
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
