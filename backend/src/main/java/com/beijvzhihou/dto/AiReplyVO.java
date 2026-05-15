package com.beijvzhihou.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiReplyVO {
    private Long id;
    private String aiProvider;       // DeepSeek / Kimi2 / Qwen
    private String perspective;       // 心情 / 历史 / 技术
    private BigDecimal emotionScore;  // 该AI评估的情绪分
    private String content;          // AI 回复内容
    private LocalDateTime createdAt;
}
