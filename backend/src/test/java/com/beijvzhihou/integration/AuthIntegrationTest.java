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
class AuthIntegrationTest {

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
        user.setEmail("auth-test@example.com");
        user.setEmailMasked("a***m@example.com");
        user.setNickname("认证测试用户");
        user.setAvatarUrl("https://picsum.photos/seed/1");
        user.setStatus(1);
        userMapper.insert(user);
        userToken = jwtUtil.generateToken(user.getId());
    }

    @Nested
    @DisplayName("认证全流程集成测试")
    class AuthFlowTest {

        @Test
        @DisplayName("发送验证码成功")
        void sendCode_success() throws Exception {
            mockMvc.perform(post("/api/auth/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"newuser@example.com\",\"type\":\"register\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("未登录访问 /api/auth/me 返回业务码 401")
        void me_withoutToken_returns401() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("有效 token 访问 /api/auth/me 返回用户信息")
        void me_withValidToken_returnsUser() throws Exception {
            mockMvc.perform(get("/api/auth/me")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.nickname").value("认证测试用户"));
        }
    }
}
