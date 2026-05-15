package com.beijvzhihou.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;
    private String emailMasked;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private String role;
    private LocalDateTime createdAt;
}
