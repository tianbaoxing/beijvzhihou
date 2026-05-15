package com.beijvzhihou.service;

import com.beijvzhihou.dto.AuthDTO;
import com.beijvzhihou.dto.RegisterDTO;
import com.beijvzhihou.dto.UserVO;
import com.beijvzhihou.entity.User;

public interface AuthService {

    /**
     * 发送邮箱验证码（注册/登录共用）
     * @param email 邮箱地址
     * @param type  类型：register / login
     */
    void sendCode(String email, String type);

    /**
     * 邮箱注册
     * @param dto 注册请求（email + code + 可选nickname）
     * @return JWT token
     */
    String register(RegisterDTO dto);

    /**
     * 邮箱+密码登录
     * @param dto 登录请求（email + password）
     * @return JWT token
     */
    String login(AuthDTO dto);

    /**
     * 获取当前用户信息
     * @param userId 用户ID（从 JWT token 解析得到）
     * @return 用户信息（脱敏）
     */
    UserVO getCurrentUser(Long userId);

    UserVO updateProfile(Long userId, String nickname);

    String getDebugCode(String email);
}
