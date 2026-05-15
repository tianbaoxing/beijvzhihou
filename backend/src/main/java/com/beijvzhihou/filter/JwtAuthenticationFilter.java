package com.beijvzhihou.filter;

import com.beijvzhihou.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器：从请求头提取 token，解析出 userId 存入 request attribute
 * 注意：这个过滤器只负责解析 token，不负责拦截（拦截由拦截器或 SecurityConfig 处理）
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTR = "userId";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtUtil.parseUserId(token);
                request.setAttribute(USER_ID_ATTR, userId);
            } catch (Exception e) {
                // token 无效或过期，不设置 userId，后续由拦截器返回 401
            }
        }
        filterChain.doFilter(request, response);
    }
}
