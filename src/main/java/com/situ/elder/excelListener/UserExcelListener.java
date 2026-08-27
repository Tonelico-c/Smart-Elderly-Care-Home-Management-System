package com.situ.elder.excelListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.pojo.vo.UserExcelVO;
import org.springframework.beans.BeanUtils;

public class UserExcelListener extends AnalysisEventListener<UserExcelVO> {

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
        userMapper.insert(user);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        System.out.println("UserExcelListener.doAfterAllAnalysed");
    }
}
