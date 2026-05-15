package com.beijvzhihou.controller;

import com.beijvzhihou.common.Result;
import com.beijvzhihou.dto.CommentCreateDTO;
import com.beijvzhihou.dto.CommentVO;
import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    @Autowired private CommentService commentService;

    @GetMapping
    public Result<PageResult<CommentVO>> list(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(commentService.listComments(postId, page, size));
    }

    @PostMapping
    public Result<CommentVO> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDTO dto,
            @RequestAttribute(JwtAuthenticationFilter.USER_ID_ATTR) Long userId) {
        return Result.ok(commentService.createComment(userId, postId, dto));
    }
}
