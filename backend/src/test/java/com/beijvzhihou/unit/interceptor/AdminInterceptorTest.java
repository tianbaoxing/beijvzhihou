package com.beijvzhihou.unit.interceptor;

import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.common.TestDataBuilder;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.interceptor.AdminInterceptor;
import com.beijvzhihou.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInterceptorTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminInterceptor adminInterceptor;

    @Mock
    private jakarta.servlet.http.HttpServletRequest request;

    @Mock
    private jakarta.servlet.http.HttpServletResponse response;

    @Nested
    @DisplayName("preHandle 权限校验")
    class PreHandleTest {

        @Test
        @DisplayName("userId 为 null 时抛出 UNAUTHORIZED 异常")
        void preHandle_noUserId_throwsUnauthorized() {
            when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> adminInterceptor.preHandle(request, response, new Object()));
            assertEquals(ResultCode.UNAUTHORIZED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("用户不存在时抛出 FORBIDDEN 异常")
        void preHandle_userNotFound_throwsForbidden() {
            when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR)).thenReturn(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> adminInterceptor.preHandle(request, response, new Object()));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("普通用户（role=USER）抛出 FORBIDDEN 异常")
        void preHandle_normalUser_throwsForbidden() {
            Long userId = 2L;
            when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR)).thenReturn(userId);

            User normalUser = TestDataBuilder.user()
                    .withId(userId)
                    .withRole("USER")
                    .build();
            when(userMapper.selectById(userId)).thenReturn(normalUser);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> adminInterceptor.preHandle(request, response, new Object()));
            assertEquals(ResultCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("管理员用户（role=ADMIN）放行返回 true")
        void preHandle_adminUser_returnsTrue() {
            Long userId = 1L;
            when(request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTR)).thenReturn(userId);

            User adminUser = TestDataBuilder.user()
                    .withId(userId)
                    .withRole("ADMIN")
                    .build();
            when(userMapper.selectById(userId)).thenReturn(adminUser);

            boolean result = adminInterceptor.preHandle(request, response, new Object());
            assertTrue(result);
        }
    }
}
