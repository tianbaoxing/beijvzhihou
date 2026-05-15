package com.beijvzhihou.interceptor;

import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("GET".equalsIgnoreCase(request.getMethod())
                && !request.getRequestURI().endsWith("/auth/me")
                && !request.getRequestURI().startsWith("/api/user/")) {
            return true;
        }

        if (request.getRequestURI().matches("/api/posts/\\d+/like")) {
            return true;
        }

        Long userId = (Long) request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return true;
    }
}
