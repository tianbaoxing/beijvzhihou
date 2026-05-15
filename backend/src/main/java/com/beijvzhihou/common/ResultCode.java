package com.beijvzhihou.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // 参数错误 400
    PARAM_ERROR(400, "参数错误"),
    
    // 认证错误 401
    UNAUTHORIZED(401, "未登录或登录已过期"),
    CODE_EXPIRED(401, "验证码已过期"),
    CODE_ERROR(401, "验证码错误"),
    
    // 权限错误 403
    FORBIDDEN(403, "无权限访问"),
    
    // 资源不存在 404
    NOT_FOUND(404, "资源不存在"),
    
    // 业务错误 422
    EMAIL_ALREADY_EXISTS(422, "该邮箱已注册"),
    EMAIL_NOT_FOUND(422, "该邮箱未注册"),
    PASSWORD_ERROR(422, "密码错误"),
    POST_NOT_FOUND(422, "帖子不存在"),
    
    // 频率限制 429
    RATE_LIMITED(429, "操作太频繁，请稍后再试"),
    ;

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
