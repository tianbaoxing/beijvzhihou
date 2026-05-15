package com.beijvzhihou.unit.util;

import com.beijvzhihou.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-32-characters-long-for-hmac-sha");
        ReflectionTestUtils.setField(jwtUtil, "expireDays", 7);
    }

    @Nested
    @DisplayName("Token 生成测试")
    class GenerateTokenTest {

        @Test
        @DisplayName("生成 token 成功")
        void generateToken_validUserId_returnsToken() {
            String token = jwtUtil.generateToken(1L);

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Token 解析测试")
    class ParseUserIdTest {

        @Test
        @DisplayName("解析 token 返回正确的 userId")
        void parseUserId_validToken_returnsUserId() {
            String token = jwtUtil.generateToken(42L);

            Long userId = jwtUtil.parseUserId(token);

            assertThat(userId).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("Token 验证测试")
    class ValidateTokenTest {

        @Test
        @DisplayName("有效 token 验证通过")
        void validateToken_validToken_returnsTrue() {
            String token = jwtUtil.generateToken(1L);

            assertThat(jwtUtil.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("无效 token 验证失败")
        void validateToken_invalidToken_returnsFalse() {
            assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
        }

        @Test
        @DisplayName("空 token 验证失败")
        void validateToken_emptyToken_returnsFalse() {
            assertThat(jwtUtil.validateToken("")).isFalse();
        }
    }
}
