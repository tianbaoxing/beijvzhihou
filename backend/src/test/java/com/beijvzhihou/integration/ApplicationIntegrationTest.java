package com.beijvzhihou.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("应用上下文加载成功")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("核心 Service Bean 已注册")
    void coreServiceBeans_areRegistered() {
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.service.AuthService.class));
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.service.PostService.class));
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.service.AIService.class));
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.service.ReviewService.class));
    }

    @Test
    @DisplayName("核心工具 Bean 已注册")
    void coreUtilBeans_areRegistered() {
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.util.JwtUtil.class));
        assertThatNoException().isThrownBy(() -> applicationContext.getBean(com.beijvzhihou.util.KeywordFilter.class));
    }
}
