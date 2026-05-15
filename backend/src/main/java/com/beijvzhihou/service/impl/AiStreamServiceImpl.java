package com.beijvzhihou.service.impl;

import com.beijvzhihou.config.AiProperties;
import com.beijvzhihou.dto.AiReplyResult;
import com.beijvzhihou.dto.AiStreamEventDTO;
import com.beijvzhihou.entity.AiReply;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.service.AiStreamService;
import com.beijvzhihou.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
public class AiStreamServiceImpl implements AiStreamService {

    private static final long SSE_TIMEOUT = 10L * 60 * 1000;
    private static final long CALL_DELAY_MS = 500;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, String> postContentMap = new ConcurrentHashMap<>();

    @Autowired private AIService aiService;
    @Autowired private AiReplyMapper aiReplyMapper;
    @Autowired private PostMapper postMapper;
    @Autowired private AiProperties aiProperties;

    @Override
    public void startAiStream(Long postId, String content) {
        postContentMap.put(postId, content);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(postId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(postId);
            postContentMap.remove(postId);
            log.info("SSE 连接完成, postId={}", postId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(postId);
            postContentMap.remove(postId);
            log.info("SSE 连接超时, postId={}", postId);
        });
        emitter.onError(e -> {
            emitters.remove(postId);
            postContentMap.remove(postId);
            log.info("SSE 连接异常, postId={}, error={}", postId, e.getMessage());
        });

        sendStart(emitter, postId);

        new Thread(() -> {
            try {
                processAiSequentially(postId, content);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void processAiSequentially(Long postId, String content) throws InterruptedException {
        SseEmitter emitter = emitters.get(postId);
        if (emitter == null) return;

        AiReplyResult deepseekResult = aiService.deepseekReply(content);
        if (deepseekResult != null && !"[跳过]".equals(deepseekResult.getContent())) {
            saveAiReply(postId, deepseekResult);
            sendDeepseekComplete(emitter, deepseekResult);
            updatePostStats(postId);
        }

        Thread.sleep(CALL_DELAY_MS);

        if (emitter == null || isEmitterDone(emitter)) return;
        int emotionThreshold = aiProperties.getEmotionThreshold();
        if (deepseekResult.getEmotionScore() != null
                && deepseekResult.getEmotionScore().compareTo(new BigDecimal(emotionThreshold)) >= 0) {

            AiReplyResult kimi2Result = callWithRetry(() -> aiService.kimi2Reply(content), "Kimi2", 2);
            if (kimi2Result != null && !"[跳过]".equals(kimi2Result.getContent())) {
                saveAiReply(postId, kimi2Result);
                sendKimi2Complete(emitter, kimi2Result);
                updatePostStats(postId);
            }

            Thread.sleep(CALL_DELAY_MS);

            if (emitter == null || isEmitterDone(emitter)) return;

            AiReplyResult qwenResult = callWithRetry(() -> aiService.qwenReply(content), "Qwen", 2);
            if (qwenResult != null && !"[跳过]".equals(qwenResult.getContent())) {
                saveAiReply(postId, qwenResult);
                sendQwenComplete(emitter, qwenResult);
                updatePostStats(postId);
            }
        }

        sendAllComplete(emitter);
    }

    private AiReplyResult callWithRetry(Supplier<AiReplyResult> supplier, String aiProvider, int maxRetries) {
        int retryCount = 0;
        long delayMs = 1000;
        
        while (retryCount <= maxRetries) {
            try {
                AiReplyResult result = supplier.get();
                if (result != null && !"[跳过]".equals(result.getContent())) {
                    return result;
                }
                return result;
            } catch (Exception e) {
                retryCount++;
                log.warn("{} 调用失败(第{}次), 尝试重试: {}", aiProvider, retryCount, e.getMessage());
                if (retryCount <= maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                        delayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("{} 调用失败, 已达最大重试次数", aiProvider);
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isEmitterDone(SseEmitter emitter) {
        return false;
    }

    private void sendStart(SseEmitter emitter, Long postId) {
        try {
            AiStreamEventDTO event = AiStreamEventDTO.start(postId);
            emitter.send(SseEmitter.event().name("start").data(event));
        } catch (IOException e) {
            log.error("SSE 发送 start 失败, postId={}", postId, e);
            emitter.complete();
        }
    }

    private void sendDeepseekComplete(SseEmitter emitter, AiReplyResult result) {
        try {
            AiStreamEventDTO event = AiStreamEventDTO.deepseekComplete(
                    result.getAiProvider(), result.getEmotionScore(), result.getContent());
            emitter.send(SseEmitter.event().name("deepseek_complete").data(event));
        } catch (IOException e) {
            log.error("SSE 发送 deepseek_complete 失败", e);
            emitter.complete();
        }
    }

    private void sendKimi2Complete(SseEmitter emitter, AiReplyResult result) {
        try {
            AiStreamEventDTO event = AiStreamEventDTO.kimi2Complete(
                    result.getAiProvider(), result.getEmotionScore(), result.getContent());
            emitter.send(SseEmitter.event().name("kimi2_complete").data(event));
        } catch (IOException e) {
            log.error("SSE 发送 kimi2_complete 失败", e);
            emitter.complete();
        }
    }

    private void sendQwenComplete(SseEmitter emitter, AiReplyResult result) {
        try {
            AiStreamEventDTO event = AiStreamEventDTO.qwenComplete(
                    result.getAiProvider(), result.getEmotionScore(), result.getContent());
            emitter.send(SseEmitter.event().name("qwen_complete").data(event));
        } catch (IOException e) {
            log.error("SSE 发送 qwen_complete 失败", e);
            emitter.complete();
        }
    }

    private void sendAllComplete(SseEmitter emitter) {
        try {
            AiStreamEventDTO event = AiStreamEventDTO.allComplete();
            emitter.send(SseEmitter.event().name("all_complete").data(event));
            emitter.complete();
        } catch (IOException e) {
            log.error("SSE 发送 all_complete 失败", e);
            emitter.complete();
        }
    }

    private void saveAiReply(Long postId, AiReplyResult result) {
        AiReply reply = new AiReply();
        reply.setPostId(postId);
        reply.setAiProvider(result.getAiProvider());
        reply.setPerspective(result.getPerspective());
        reply.setEmotionScore(result.getEmotionScore());
        reply.setContent(result.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        aiReplyMapper.insert(reply);
    }

    private void updatePostStats(Long postId) {
        try {
            Long count = aiReplyMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiReply>()
                            .eq(AiReply::getPostId, postId));
            var post = postMapper.selectById(postId);
            if (post != null && count != null) {
                post.setAiResponseCount(count.intValue());
                var avg = aiReplyMapper.selectAvgEmotionScore(postId);
                if (avg != null) {
                    post.setEmotionScoreAvg(avg);
                }
                postMapper.updateById(post);
            }
        } catch (Exception e) {
            log.error("更新帖子 AI 统计失败, postId={}", postId, e);
        }
    }

    @Override
    public SseEmitter getEmitter(Long postId) {
        return emitters.get(postId);
    }
}
