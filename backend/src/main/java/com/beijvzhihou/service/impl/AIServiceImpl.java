package com.beijvzhihou.service.impl;

import com.beijvzhihou.config.AiProperties;
import com.beijvzhihou.dto.AiReplyResult;
import com.beijvzhihou.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private static final String DEEPSEEK_PROMPT = """
            你是一个温暖的朋友。用户刚刚收到了求职被拒的消息。
            请从情感共鸣的角度，给予真诚的安慰和陪伴。
            不要说什么教，不要给建议，只是陪伴和认可对方的感受。
            语气温暖，像一个真正理解他的朋友。

            首先评估用户的情绪强度（1-10分），如果情绪很激动或低落，给出7-8分；如果一般低落，给出5-6分；如果情绪还好，给出3-4分。

            然后按照以下格式回复（严格按此格式，不要有其他内容）：
            [情绪分:X]
            [回复内容]
            """;

    private static final String KIMI2_PROMPT = """
            你是一个博学的好友。请阅读下面的求职者的倾诉帖子。
            首先评估他的情绪强度（1-10分）。
            - 如果情绪分 >= 2，请举1-2个历史上或名人中求职/人生被拒后最终成功的真实例子。
            - 如果情绪分 < 2，只回复"[跳过]"即可，不要回复其他内容。

            语气真诚，既不空洞鼓励，也要让对方感受到"被理解"。

            然后按照以下格式回复（严格按此格式）：
            [情绪分:X]
            [回复内容（如果情绪分>=2）]
            """;

    private static final String QWEN_PROMPT = """
            你是一个资深的技术面试官和职业顾问。请阅读下面的帖子。
            首先评估发帖人的情绪强度（1-10分）。
            - 如果情绪分 >= 2，请从简历优化、面试技巧、行业选择等角度给出1-2条实际可行的方向性建议。
            - 如果情绪分 < 2，只回复"[跳过]"即可，不要回复其他内容。

            语气专业但温和，既要实用，也不要让对方觉得你在说教。

            然后按照以下格式回复（严格按此格式）：
            [情绪分:X]
            [回复内容（如果情绪分>=2）]
            """;

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    @Qualifier("deepseekChatModel")
    private ChatModel deepseekChatModel;

    @Autowired
    @Qualifier("kimi2ChatModel")
    private ChatModel kimi2ChatModel;

    @Autowired
    @Qualifier("qwenChatModel")
    private ChatModel qwenChatModel;

    @Override
    public AiReplyResult deepseekReply(String content) {
        return callWithSpringAi(deepseekChatModel, DEEPSEEK_PROMPT, content, "DeepSeek", "心情",
                new BigDecimal("7.0"), "【降级】抱抱你。被拒绝真的很让人难过，但请相信，这绝不是你的终点。");
    }

    @Override
    public AiReplyResult kimi2Reply(String content) {
        return callWithSpringAi(kimi2ChatModel, KIMI2_PROMPT, content, "Kimi2", "历史",
                new BigDecimal("5.0"), "[跳过]");
    }

    @Override
    public AiReplyResult qwenReply(String content) {
        return callWithSpringAi(qwenChatModel, QWEN_PROMPT, content, "Qwen", "技术",
                new BigDecimal("5.0"), "[跳过]");
    }

    private AiReplyResult callWithSpringAi(ChatModel chatModel, String systemPrompt,
                                            String userContent, String aiProvider, String perspective,
                                            BigDecimal fallbackScore, String fallbackContent) {
        try {
            log.info("===== 开始调用 {} 模型 =====", aiProvider);
            
            // 获取模型详情
            String modelName = "unknown";
            String baseUrl = "unknown";
            if (chatModel instanceof OpenAiChatModel) {
                OpenAiChatModel openAiModel = (OpenAiChatModel) chatModel;
                OpenAiChatOptions options = (OpenAiChatOptions) openAiModel.getDefaultOptions();
                if (options != null && options.getModel() != null) {
                    modelName = options.getModel();
                }
                // 从配置中获取 baseUrl
                AiProperties.AiProvider providerConfig = getProviderConfig(aiProvider);
                if (providerConfig != null && providerConfig.getBaseUrl() != null) {
                    baseUrl = providerConfig.getBaseUrl();
                }
            }
            
            log.info("模型名称: {}", modelName);
            log.info("API 地址: {}", baseUrl);
            log.info("提示词长度: System={}, User={} chars", systemPrompt.length(), userContent.length());
            log.info("系统提示词: {}", systemPrompt.substring(0, Math.min(150, systemPrompt.length())));
            log.info("用户内容: {}", userContent.substring(0, Math.min(200, userContent.length())));

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt.trim()),
                    new UserMessage("用户发帖内容：\n" + userContent)
            ));

            log.info("正在请求模型...");
            long startTime = System.currentTimeMillis();
            
            String aiResponse = chatModel.call(prompt).getResult().getOutput().getText();
            
            long endTime = System.currentTimeMillis();
            log.info("模型响应耗时: {} ms", (endTime - startTime));
            log.info("{} 回复成功 (长度: {} chars): {}", aiProvider, aiResponse.length(),
                    aiResponse.substring(0, Math.min(300, aiResponse.length())));
            log.info("===== {} 调用完成 =====", aiProvider);
            
            return parseAiResponse(aiProvider, perspective, aiResponse);
        } catch (Exception e) {
            log.error("===== {} 调用失败 =====", aiProvider);
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误消息: {}", e.getMessage(), e);
            log.error("使用降级回复: {}", fallbackContent);
            log.error("========================");
            return createMockReply(aiProvider, perspective, fallbackScore, fallbackContent);
        }
    }
    
    private AiProperties.AiProvider getProviderConfig(String aiProvider) {
        switch (aiProvider) {
            case "DeepSeek":
                return aiProperties.getDeepseek();
            case "Kimi2":
                return aiProperties.getKimi2();
            case "Qwen":
                return aiProperties.getQwen();
            default:
                return null;
        }
    }

    @Override
    public List<AiReplyResult> generateReplies(String content) {
        List<AiReplyResult> results = new ArrayList<>();

        AiReplyResult deepseekResult = deepseekReply(content);
        if (deepseekResult != null && !"[跳过]".equals(deepseekResult.getContent())) {
            results.add(deepseekResult);
        }

        int emotionThreshold = aiProperties.getEmotionThreshold();

        AiReplyResult kimi2Result = kimi2Reply(content);
        if (kimi2Result != null && kimi2Result.getEmotionScore() != null
                && kimi2Result.getEmotionScore().compareTo(new BigDecimal(emotionThreshold)) >= 0
                && !"[跳过]".equals(kimi2Result.getContent())) {
            results.add(kimi2Result);
        }

        AiReplyResult qwenResult = qwenReply(content);
        if (qwenResult != null && qwenResult.getEmotionScore() != null
                && qwenResult.getEmotionScore().compareTo(new BigDecimal(emotionThreshold)) >= 0
                && !"[跳过]".equals(qwenResult.getContent())) {
            results.add(qwenResult);
        }

        return results;
    }

    private AiReplyResult parseAiResponse(String aiProvider, String perspective, String response) {
        try {
            String emotionScoreStr = null;
            String content = response;

            if (response.contains("[情绪分:")) {
                int start = response.indexOf("[情绪分:") + 5;
                int end = response.indexOf("]", start);
                if (end > start) {
                    emotionScoreStr = response.substring(start, end).trim();
                    content = response.substring(end + 1).trim();
                }
            }

            BigDecimal emotionScore = emotionScoreStr != null
                    ? new BigDecimal(emotionScoreStr)
                    : new BigDecimal("5.0");

            return AiReplyResult.of(aiProvider, perspective, emotionScore, content);
        } catch (Exception e) {
            log.warn("解析 AI 响应失败，使用默认值: {}", response, e);
            return createMockReply(aiProvider, perspective, new BigDecimal("5.0"), response);
        }
    }

    private AiReplyResult createMockReply(String aiProvider, String perspective, BigDecimal emotionScore, String content) {
        return AiReplyResult.of(aiProvider, perspective, emotionScore, content);
    }
}
