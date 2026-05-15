package com.beijvzhihou.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private List<CommentVO> replies;
}
