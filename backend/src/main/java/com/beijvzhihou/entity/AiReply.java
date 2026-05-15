package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_reply")
public class AiReply {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("post_id")
    private Long postId;
    
    @TableField("ai_type")
    private String aiProvider;     // DeepSeek / Kimi2 / Qwen
    
    @TableField("reply_content")
    private String content;        // AI 回复内容
    
    @TableField("trigger_score")
    private BigDecimal emotionScore; // 该AI评估的情绪分
    
    @TableField(exist = false)
    private String perspective;     // 心情 / 历史 / 技术
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    public String getPerspective() {
        if (perspective != null) return perspective;
        if ("DeepSeek".equals(aiProvider)) return "心情";
        if ("Kimi2".equals(aiProvider)) return "历史";
        if ("Qwen".equals(aiProvider)) return "技术";
        return "未知";
    }
    
    public void setPerspective(String perspective) {
        this.perspective = perspective;
    }
}