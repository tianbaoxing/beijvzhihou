package com.beijvzhihou.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiStreamService {

    void startAiStream(Long postId, String content);

    SseEmitter getEmitter(Long postId);
}
