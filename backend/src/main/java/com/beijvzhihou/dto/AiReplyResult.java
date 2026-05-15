package com.beijvzhihou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReplyResult {
    private String aiProvider;
    private String perspective;
    private BigDecimal emotionScore;
    private String content;

    public static AiReplyResult of(String aiProvider, String perspective, BigDecimal emotionScore, String content) {
        return new AiReplyResult(aiProvider, perspective, emotionScore, content);
    }
}