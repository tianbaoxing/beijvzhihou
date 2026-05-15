package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_like")
public class PostLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    private Long userId;    // NULL = 匿名点赞
    private String ipHash;  // IP哈希，用于未登录点赞去重
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
