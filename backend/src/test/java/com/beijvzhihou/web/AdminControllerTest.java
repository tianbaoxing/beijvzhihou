package com.beijvzhihou.web;

import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.common.TestDataBuilder;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.interceptor.AdminInterceptor;
import com.beijvzhihou.interceptor.AuthInterceptor;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.CommentMapper;
import com.beijvzhihou.mapper.ContentReviewMapper;
import com.beijvzhihou.mapper.EmailCodeMapper;
import com.beijvzhihou.mapper.PostLikeMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.ReviewService;
import com.beijvzhihou.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
        controllers = com.beijvzhihou.controller.AdminController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        }
)
@Import({JwtAuthenticationFilter.class, JwtUtil.class, com.beijvzhihou.common.GlobalExceptionHandler.class})
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @MockitoBean
    private AdminInterceptor adminInterceptor;

    @MockitoBean
    private AiReplyMapper aiReplyMapper;

    @MockitoBean
    private PostLikeMapper postLikeMapper;

    @MockitoBean
    private ContentReviewMapper contentReviewMapper;

    @MockitoBean
    private EmailCodeMapper emailCodeMapper;

    @MockitoBean
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                .thenReturn(true);
    }

    @Nested
    @DisplayName("管理员权限校验")
    class AdminPermissionTest {

        @Test
        @DisplayName("未登录访问管理接口返回 401")
        void adminEndpoint_noToken_returns401() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenThrow(new BusinessException(ResultCode.UNAUTHORIZED));

            mockMvc.perform(get("/api/admin/review/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("普通用户访问管理接口返回 403")
        void adminEndpoint_normalUser_returns403() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenThrow(new BusinessException(ResultCode.FORBIDDEN));

            mockMvc.perform(get("/api/admin/review/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("管理员访问管理接口返回 200")
        void adminEndpoint_adminUser_returns200() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenReturn(true);
            when(postMapper.selectCount(null)).thenReturn(100L);
            when(userMapper.selectCount(null)).thenReturn(50L);

            mockMvc.perform(get("/api/admin/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("管理员统计接口")
    class StatsTest {

        @BeforeEach
        void setUpAdmin() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("管理员获取统计数据成功")
        void stats_success() throws Exception {
            when(postMapper.selectCount(null)).thenReturn(100L);
            when(userMapper.selectCount(null)).thenReturn(50L);

            mockMvc.perform(get("/api/admin/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalPosts").value(100))
                    .andExpect(jsonPath("$.data.totalUsers").value(50));
        }
    }

    @Nested
    @DisplayName("管理员审核接口")
    class ReviewTest {

        @BeforeEach
        void setUpAdmin() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("管理员审核帖子成功")
        void review_success() throws Exception {
            mockMvc.perform(post("/api/admin/review/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"result\":\"approve\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("普通用户审核帖子返回 403")
        void review_normalUser_returns403() throws Exception {
            when(adminInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenThrow(new BusinessException(ResultCode.FORBIDDEN));

            mockMvc.perform(post("/api/admin/review/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"result\":\"approve\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(403));
        }
    }
}
