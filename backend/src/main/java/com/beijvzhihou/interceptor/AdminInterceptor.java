package com.beijvzhihou.interceptor;

import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = (Long) request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return true;
    }
}
