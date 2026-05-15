package com.beijvzhihou.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CommentCreateDTO {
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 200, message = "评论内容不能超过200字")
    private String content;

    private Long parentId;
}
