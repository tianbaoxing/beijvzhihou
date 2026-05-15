package com.beijvzhihou.unit.service;

import com.beijvzhihou.config.AiProperties;
import com.beijvzhihou.dto.AiReplyResult;
import com.beijvzhihou.service.impl.AIServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Spy
    private AiProperties aiProperties = createTestAiProperties();

    @InjectMocks
    private AIServiceImpl aiService;

    private static AiProperties createTestAiProperties() {
        AiProperties props = new AiProperties();
        props.setEmotionThreshold(6);
        props.setDeepseek(new AiProperties.AiProvider());
        props.setKimi2(new AiProperties.AiProvider());
        props.setQwen(new AiProperties.AiProvider());
        return props;
    }

    @Nested
    @DisplayName("DeepSeek 回复测试")
    class DeepseekReplyTest {

        @Test
        @DisplayName("未配置 API Key 时返回模拟回复")
        void deepseekReply_withoutApiKey_returnsMockReply() {
            AiReplyResult result = aiService.deepseekReply("今天面试被拒了，好难过");

            assertThat(result).isNotNull();
            assertThat(result.getAiProvider()).isEqualTo("DeepSeek");
            assertThat(result.getPerspective()).isEqualTo("心情");
            assertThat(result.getContent()).contains("【模拟】");
            assertThat(result.getEmotionScore()).isEqualTo(new BigDecimal("7.0"));
        }
    }

    @Nested
    @DisplayName("Kimi2 回复测试")
    class Kimi2ReplyTest {

        @Test
        @DisplayName("未配置 API Key 时返回[跳过]")
        void kimi2Reply_withoutApiKey_returnsSkipped() {
            AiReplyResult result = aiService.kimi2Reply("今天面试被拒了，好难过");

            assertThat(result).isNotNull();
            assertThat(result.getAiProvider()).isEqualTo("Kimi2");
            assertThat(result.getPerspective()).isEqualTo("历史");
            assertThat(result.getContent()).isEqualTo("[跳过]");
            assertThat(result.getEmotionScore()).isEqualTo(new BigDecimal("5.0"));
        }
    }

    @Nested
    @DisplayName("Qwen 回复测试")
    class QwenReplyTest {

        @Test
        @DisplayName("未配置 API Key 时返回[跳过]")
        void qwenReply_withoutApiKey_returnsSkipped() {
            AiReplyResult result = aiService.qwenReply("今天面试被拒了，好难过");

            assertThat(result).isNotNull();
            assertThat(result.getAiProvider()).isEqualTo("Qwen");
            assertThat(result.getPerspective()).isEqualTo("技术");
            assertThat(result.getContent()).isEqualTo("[跳过]");
            assertThat(result.getEmotionScore()).isEqualTo(new BigDecimal("5.0"));
        }
    }

    @Nested
    @DisplayName("多 AI 回复生成测试")
    class GenerateRepliesTest {

        @Test
        @DisplayName("生成 AI 回复")
        void generateReplies_returnsValidReplies() {
            List<AiReplyResult> results = aiService.generateReplies("今天面试被拒了，好难过");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getAiProvider()).isEqualTo("DeepSeek");
        }
    }
}
