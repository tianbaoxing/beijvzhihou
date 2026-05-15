package com.beijvzhihou.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private BigDecimal emotionScoreAvg;
    private Integer likeCount;
    private Integer viewCount;
    private Integer aiResponseCount;
    private Integer commentCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private List<AiReplyVO> aiReplies;
}
