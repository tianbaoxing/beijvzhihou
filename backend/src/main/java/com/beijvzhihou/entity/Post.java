package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String content;
    private BigDecimal emotionScoreAvg;
    private Integer aiResponseCount;
    private Integer commentCount;
    private Integer likeCount;
    private Integer viewCount;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
