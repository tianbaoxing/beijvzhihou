package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("content_review")
public class ContentReview {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private String reviewType;  // keyword / ai / manual
    private String result;       // pass / reject
    private String reason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
