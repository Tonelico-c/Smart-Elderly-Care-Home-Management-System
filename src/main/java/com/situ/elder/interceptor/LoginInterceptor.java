package com.situ.elder.interceptor;

import com.situ.elder.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(token)) {
            response.setStatus(401);
            return false;
        }
        try{
            Map<String, Object> map = JwtUtil.parseToken(token);
            return true;
        } catch(Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            return false;
        }
    }
}
