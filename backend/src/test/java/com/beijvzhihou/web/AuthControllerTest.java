package com.beijvzhihou.web;

import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.interceptor.AuthInterceptor;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.ContentReviewMapper;
import com.beijvzhihou.mapper.EmailCodeMapper;
import com.beijvzhihou.mapper.PostLikeMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.AuthService;
import com.beijvzhihou.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = com.beijvzhihou.controller.AuthController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        }
)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @MockitoBean
    private AiReplyMapper aiReplyMapper;

    @MockitoBean
    private PostLikeMapper postLikeMapper;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private ContentReviewMapper contentReviewMapper;

    @MockitoBean
    private EmailCodeMapper emailCodeMapper;

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                .thenReturn(true);
    }

    @Nested
    @DisplayName("发送验证码接口测试")
    class SendCodeTest {

        @Test
        @DisplayName("发送验证码成功")
        void sendCode_success() throws Exception {
            mockMvc.perform(post("/api/auth/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@example.com\",\"type\":\"login\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("注册接口测试")
    class RegisterTest {

        @Test
        @DisplayName("注册成功返回 token")
        void register_success() throws Exception {
            when(authService.register(any())).thenReturn("test-jwt-token");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"nickname\":\"测试\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
        }
    }

    @Nested
    @DisplayName("登录接口测试")
    class LoginTest {

        @Test
        @DisplayName("登录成功返回 token")
        void login_success() throws Exception {
            when(authService.login(any())).thenReturn("test-jwt-token");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@example.com\",\"code\":\"123456\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
        }
    }

    @Nested
    @DisplayName("获取当前用户接口测试")
    class GetCurrentUserTest {

        @Test
        @DisplayName("未登录时返回业务码 401")
        void getCurrentUser_noAuth_returns401() throws Exception {
            when(authInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenThrow(new com.beijvzhihou.common.BusinessException(com.beijvzhihou.common.ResultCode.UNAUTHORIZED));

            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }
}
