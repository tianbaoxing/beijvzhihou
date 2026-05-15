package com.beijvzhihou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;           // AES加密存储原文
    private String emailMasked;    // 脱敏展示: c***a@qq.com
    private String nickname;        // 随机匿名昵称
    private String avatarUrl;       // 随机头像 URL
    private String passwordHash;   // BCrypt 加密密码
    private Integer status;          // 1=正常 0=禁用
    private String role;             // USER=普通用户 ADMIN=管理员
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
