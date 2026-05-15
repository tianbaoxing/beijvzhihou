package com.beijvzhihou.integration;

import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("post-test@example.com");
        user.setEmailMasked("p***t@example.com");
        user.setNickname("发帖测试用户");
        user.setAvatarUrl("https://picsum.photos/seed/2");
        user.setStatus(1);
        userMapper.insert(user);
        userToken = jwtUtil.generateToken(user.getId());
    }

    @Nested
    @DisplayName("帖子列表集成测试")
    class ListPostsTest {

        @Test
        @DisplayName("获取帖子列表成功")
        void listPosts_returnsPageResult() throws Exception {
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("发帖集成测试")
    class CreatePostTest {

        @Test
        @DisplayName("未登录发帖返回业务码 401")
        void createPost_withoutToken_returns401() throws Exception {
            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"今天面试被拒了\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("登录后发帖成功")
        void createPost_withToken_returnsPostVO() throws Exception {
            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"今天面试被拒了，好难过\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("今天面试被拒了，好难过"));
        }
    }

    @Nested
    @DisplayName("帖子详情集成测试")
    class PostDetailTest {

        @Test
        @DisplayName("获取帖子详情成功")
        void getPost_validId_returnsPostVO() throws Exception {
            String responseBody = mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"集成测试帖子\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn().getResponse().getContentAsString();

            Object postIdObj = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.id");
            String postId = String.valueOf(postIdObj);

            mockMvc.perform(get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
