package com.beijvzhihou.controller;

import com.beijvzhihou.common.Result;
import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.dto.PostVO;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private PostService postService;

    @GetMapping("/posts")
    public Result<PageResult<PostVO>> myPosts(
            @RequestAttribute(JwtAuthenticationFilter.USER_ID_ATTR) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.listUserPosts(userId, page, size));
    }
}