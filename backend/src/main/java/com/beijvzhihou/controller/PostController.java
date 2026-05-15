package com.beijvzhihou.controller;

import com.beijvzhihou.common.Result;
import com.beijvzhihou.dto.AiStreamEventDTO;
import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.dto.PostCreateDTO;
import com.beijvzhihou.dto.PostVO;
import com.beijvzhihou.filter.JwtAuthenticationFilter;
import com.beijvzhihou.service.AiStreamService;
import com.beijvzhihou.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired private PostService postService;
    @Autowired private AiStreamService aiStreamService;

    @GetMapping
    public Result<PageResult<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "time") String sort) {
        return Result.ok(postService.listPosts(page, size, sort));
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(
            @PathVariable Long id,
            @RequestAttribute(value = JwtAuthenticationFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestParam(value = "fingerprint", required = false, defaultValue = "") String fingerprint) {
        return Result.ok(postService.getPost(id, userId, fingerprint));
    }

    @PostMapping
    public Result<PostVO> create(
            @Valid @RequestBody PostCreateDTO dto,
            @RequestAttribute(JwtAuthenticationFilter.USER_ID_ATTR) Long userId) {
        PostVO postVO = postService.createPostAndStartAiStream(userId, dto);
        return Result.ok(postVO);
    }

    @GetMapping(value = "/{id}/ai-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter aiStream(@PathVariable Long id) {
        return aiStreamService.getEmitter(id);
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestAttribute(value = JwtAuthenticationFilter.USER_ID_ATTR, required = false) Long userId) {
        String fingerprint = body.get("fingerprint") != null ? body.get("fingerprint").toString() : "";
        postService.toggleLike(id, userId, fingerprint);
        return Result.ok();
    }
}
