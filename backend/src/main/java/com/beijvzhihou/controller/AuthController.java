package com.beijvzhihou.controller;

import com.beijvzhihou.common.Result;
import com.beijvzhihou.dto.AuthDTO;
import com.beijvzhihou.dto.RegisterDTO;
import com.beijvzhihou.dto.UserVO;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        authService.sendCode(body.get("email"), body.get("type"));
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody RegisterDTO dto) {
        String token = authService.register(dto);
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody AuthDTO dto) {
        String token = authService.login(dto);
        return Result.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestAttribute(JwtAuthenticationFilter.USER_ID_ATTR) Long userId) {
        return Result.ok(authService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    public Result<UserVO> updateProfile(@RequestAttribute(JwtAuthenticationFilter.USER_ID_ATTR) Long userId,
                                        @RequestBody Map<String, String> body) {
        return Result.ok(authService.updateProfile(userId, body.get("nickname")));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    @GetMapping("/debug/code")
    public Result<String> debugGetCode(@RequestParam String email) {
        return Result.ok(authService.getDebugCode(email));
    }
}
