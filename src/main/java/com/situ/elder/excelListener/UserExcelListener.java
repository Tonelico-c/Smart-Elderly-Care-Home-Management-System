package com.situ.elder.excelListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.pojo.vo.UserExcelVO;
import com.situ.elder.utils.PasswordUtil;
import org.springframework.beans.BeanUtils;

public class UserExcelListener extends AnalysisEventListener<UserExcelVO> {

    /**
     * 导入用户的初始密码。
     * 导入模板里不再包含密码列（避免导出时把密码哈希带出去），
     * 所以导入的用户统一用这个初始密码，由本人登录后自行修改。
     */
    private static final String DEFAULT_PASSWORD = "123456";

    private UserMapper userMapper;

    public UserExcelListener(UserMapper userMapper){
        this.userMapper = userMapper;
    }

    @Override
    public void invoke(UserExcelVO userExcelVO, AnalysisContext analysisContext) {
        System.out.println("UserExcelListener.invoke");
        User user = new User();
        BeanUtils.copyProperties(userExcelVO, user);
        // 避免导入时id冲突
        user.setId(null);
        // 初始密码统一设置，并且必须加密后再入库
        user.setPassword(PasswordUtil.hash(DEFAULT_PASSWORD));
        userMapper.insert(user);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        System.out.println("UserExcelListener.doAfterAllAnalysed");
    }
}
