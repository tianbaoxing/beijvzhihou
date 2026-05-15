package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_code")
public class EmailCode {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;
    private String code;
    private String type;           // register / login / reset
    private LocalDateTime expiresAt;
    private Integer used;           // 0=未用 1=已用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
