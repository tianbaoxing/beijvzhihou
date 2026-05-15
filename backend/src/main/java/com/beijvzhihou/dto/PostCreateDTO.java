package com.beijvzhihou.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class PostCreateDTO {
    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "内容不能超过500字")
    private String content;
}
