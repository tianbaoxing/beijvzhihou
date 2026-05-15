package com.beijvzhihou.unit.service;

import com.beijvzhihou.dto.UserVO;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.impl.AuthServiceImpl;
import com.beijvzhihou.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "测试用户";

    @Nested
    @DisplayName("发送验证码测试")
    class SendCodeTest {

        @Test
        @DisplayName("发送登录验证码成功")
        void sendCode_loginType_success() {
            authService.sendCode(TEST_EMAIL, "login");
        }

        @Test
        @DisplayName("发送注册验证码成功")
        void sendCode_registerType_success() {
            authService.sendCode(TEST_EMAIL, "register");
        }
    }

    @Nested
    @DisplayName("获取当前用户测试")
    class GetCurrentUserTest {

        @Test
        @DisplayName("获取用户信息成功")
        void getCurrentUser_validUserId_returnsUserVO() {
            when(userMapper.selectById(1L)).thenReturn(createTestUser());

            UserVO userVO = authService.getCurrentUser(1L);

            assertThat(userVO).isNotNull();
            assertThat(userVO.getId()).isEqualTo(1L);
            assertThat(userVO.getNickname()).isEqualTo(TEST_NICKNAME);
        }
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail(TEST_EMAIL);
        user.setNickname(TEST_NICKNAME);
        user.setAvatarUrl("https://picsum.photos/seed/123");
        user.setStatus(1);
        return user;
    }
}
