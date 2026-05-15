package com.beijvzhihou.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beijvzhihou.common.Result;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private ReviewService reviewService;
    @Autowired private PostMapper postMapper;
    @Autowired private UserMapper userMapper;

    @GetMapping("/review/pending")
    public Result<List<Post>> pendingPosts() {
        return Result.ok(reviewService.getPendingPosts());
    }

    @PostMapping("/review/{id}")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reviewService.reviewPost(id, body.get("result"));
        return Result.ok();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long totalPosts = postMapper.selectCount(null);
        long totalUsers = userMapper.selectCount(null);
        long todayPosts = postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .ge(Post::getCreatedAt, LocalDate.now().atStartOfDay())
        );
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", totalPosts);
        stats.put("totalUsers", totalUsers);
        stats.put("todayPosts", todayPosts);
        return Result.ok(stats);
    }

    @GetMapping("/users")
    public Result<IPage<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<User> userPage = userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt)
        );
        userPage.getRecords().forEach(u -> u.setPasswordHash(null));
        return Result.ok(userPage);
    }

    @GetMapping("/posts")
    public Result<IPage<Post>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt);
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        IPage<Post> postPage = postMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.ok(postPage);
    }
}