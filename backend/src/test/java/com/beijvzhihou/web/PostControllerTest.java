package com.beijvzhihou.web;

import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.dto.PostVO;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.interceptor.AuthInterceptor;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.ContentReviewMapper;
import com.beijvzhihou.mapper.EmailCodeMapper;
import com.beijvzhihou.mapper.PostLikeMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.PostService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = com.beijvzhihou.controller.PostController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        }
)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

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
    @DisplayName("帖子列表接口测试")
    class ListPostsTest {

        @Test
        @DisplayName("获取帖子列表成功")
        void listPosts_defaultParams_returnsPageResult() throws Exception {
            PageResult<PostVO> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
            when(postService.listPosts(anyInt(), anyInt(), anyString())).thenReturn(pageResult);

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按热度排序获取帖子列表")
        void listPosts_hotSort_returnsPageResult() throws Exception {
            PageResult<PostVO> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
            when(postService.listPosts(anyInt(), anyInt(), anyString())).thenReturn(pageResult);

            mockMvc.perform(get("/api/posts?sort=hot"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("帖子详情接口测试")
    class DetailTest {

        @Test
        @DisplayName("获取帖子详情成功")
        void detail_validId_returnsPostVO() throws Exception {
            PostVO postVO = new PostVO();
            postVO.setId(1L);
            postVO.setContent("今天面试被拒了");
            when(postService.getPost(anyLong(), any(), anyString())).thenReturn(postVO);

            mockMvc.perform(get("/api/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Nested
    @DisplayName("发帖接口测试")
    class CreatePostTest {

        @Test
        @DisplayName("未登录时发帖返回业务码 401")
        void createPost_noAuth_returns401() throws Exception {
            when(authInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
                    .thenThrow(new com.beijvzhihou.common.BusinessException(com.beijvzhihou.common.ResultCode.UNAUTHORIZED));

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"今天面试被拒了\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }
}
