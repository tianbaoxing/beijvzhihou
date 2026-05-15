package com.beijvzhihou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.dto.AuthDTO;
import com.beijvzhihou.dto.RegisterDTO;
import com.beijvzhihou.dto.UserVO;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.AuthService;
import com.beijvzhihou.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务实现（Redis-free MVP 版）
 * 验证码存储在内存 ConcurrentHashMap，生产环境请替换为 Redis
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired private UserMapper userMapper;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 内存存储验证码：key=email, value={code, expireTime} */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    private static final String[] NICKNAMES = {
            "伤心的向日葵", "不服输的猫", "追光的蜗牛", "倔强的小草",
            "不服命的星星", "沉默的萤火虫", "倔强的刺猬", "失意的向日葵",
            "奔跑的企鹅", "迷茫的小鹿"
    };

    private static final String AVATAR_BASE = "https://picsum.photos/seed/";
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000L; // 5分钟

    private record CodeEntry(String code, long expireTime) {}

    @Override
    public void sendCode(String email, String type) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        codeStore.put(email, new CodeEntry(code, System.currentTimeMillis() + CODE_EXPIRE_MS));
        // MVP：日志输出验证码（后续替换为真实邮件发送）
        log.info("【MOCK邮件】发送验证码到 {}，验证码：{}，类型：{}", email, code, type);
    }

    @Override
    public String register(RegisterDTO dto) {
        // 1. 验证验证码
        String storedCode = getValidCode(dto.getEmail());
        if (storedCode == null || !storedCode.equals(dto.getCode())) {
            throw new BusinessException(ResultCode.CODE_ERROR);
        }
        codeStore.remove(dto.getEmail()); // 一次性使用

        // 2. 检查邮箱是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        if (userMapper.selectCount(wrapper).intValue() > 0) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. 生成用户
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setEmailMasked(maskEmail(dto.getEmail()));
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname())
                ? dto.getNickname()
                : NICKNAMES[new Random().nextInt(NICKNAMES.length)]);
        user.setAvatarUrl(AVATAR_BASE + new Random().nextInt(1000));
        user.setStatus(1);
        user.setRole("USER");
        userMapper.insert(user);

        // 4. 返回 JWT token
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public String login(AuthDTO dto) {
        // 1. 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.EMAIL_NOT_FOUND);
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 返回 JWT token
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return new UserVO(user.getId(), user.getEmailMasked(),
                user.getNickname(), user.getAvatarUrl(), user.getStatus(), user.getRole(), user.getCreatedAt());
    }

    @Override
    public UserVO updateProfile(Long userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
            userMapper.updateById(user);
        }
        return new UserVO(user.getId(), user.getEmailMasked(),
                user.getNickname(), user.getAvatarUrl(), user.getStatus(), user.getRole(), user.getCreatedAt());
    }

    /** 取出有效验证码（未过期则返回，否则返回null） */
    private String getValidCode(String email) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null || System.currentTimeMillis() > entry.expireTime()) {
            codeStore.remove(email);
            return null;
        }
        return entry.code();
    }

    @Override
    public String getDebugCode(String email) {
        return getValidCode(email);
    }

    /** 邮箱脱敏：conca@qq.com → c***a@qq.com */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "***" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
