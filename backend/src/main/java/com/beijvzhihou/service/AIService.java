package com.beijvzhihou.service;

import com.beijvzhihou.dto.AiReplyResult;

import java.util.List;

public interface AIService {

    AiReplyResult deepseekReply(String content);

    AiReplyResult kimi2Reply(String content);

    AiReplyResult qwenReply(String content);

    List<AiReplyResult> generateReplies(String content);
}