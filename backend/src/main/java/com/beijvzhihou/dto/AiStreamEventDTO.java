package com.beijvzhihou.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AiStreamEventDTO {

    public enum EventType {
        START,
        DEEPSEEK_COMPLETE,
        KIMI2_COMPLETE,
        QWEN_COMPLETE,
        ALL_COMPLETE,
        ERROR
    }

    private EventType eventType;
    private String aiProvider;
    private BigDecimal emotionScore;
    private String content;
    private String message;
    private LocalDateTime createdAt;

    public static AiStreamEventDTO start(Long postId) {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.START;
        e.message = "DeepSeek 正在回复...";
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public static AiStreamEventDTO deepseekComplete(String aiProvider, BigDecimal emotionScore, String content) {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.DEEPSEEK_COMPLETE;
        e.aiProvider = aiProvider;
        e.emotionScore = emotionScore;
        e.content = content;
        e.message = "Kimi2 正在回复...";
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public static AiStreamEventDTO kimi2Complete(String aiProvider, BigDecimal emotionScore, String content) {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.KIMI2_COMPLETE;
        e.aiProvider = aiProvider;
        e.emotionScore = emotionScore;
        e.content = content;
        e.message = "Qwen 正在回复...";
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public static AiStreamEventDTO qwenComplete(String aiProvider, BigDecimal emotionScore, String content) {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.QWEN_COMPLETE;
        e.aiProvider = aiProvider;
        e.emotionScore = emotionScore;
        e.content = content;
        e.message = "AI 回复全部完成";
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public static AiStreamEventDTO allComplete() {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.ALL_COMPLETE;
        e.message = "AI 回复全部完成";
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public static AiStreamEventDTO error(String message) {
        AiStreamEventDTO e = new AiStreamEventDTO();
        e.eventType = EventType.ERROR;
        e.message = message;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public EventType getEventType() { return eventType; }
    public String getAiProvider() { return aiProvider; }
    public BigDecimal getEmotionScore() { return emotionScore; }
    public String getContent() { return content; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
