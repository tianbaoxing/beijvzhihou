package com.beijvzhihou.common;

import com.beijvzhihou.entity.AiReply;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestDataBuilder {

    private TestDataBuilder() {}

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static PostBuilder post() {
        return new PostBuilder();
    }

    public static AiReplyBuilder aiReply() {
        return new AiReplyBuilder();
    }

    public static class UserBuilder {
        private Long id = 1L;
        private String email = "test@example.com";
        private String emailMasked = "t***e@example.com";
        private String nickname = "测试用户";
        private String avatarUrl = "https://picsum.photos/seed/123";
        private Integer status = 1;
        private String role = "USER";

        public UserBuilder withId(Long id) { this.id = id; return this; }
        public UserBuilder withEmail(String email) { this.email = email; return this; }
        public UserBuilder withNickname(String nickname) { this.nickname = nickname; return this; }
        public UserBuilder withStatus(Integer status) { this.status = status; return this; }
        public UserBuilder withRole(String role) { this.role = role; return this; }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setEmailMasked(emailMasked);
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setStatus(status);
            user.setRole(role);
            return user;
        }
    }

    public static class PostBuilder {
        private Long id = 1L;
        private Long userId = 1L;
        private String content = "今天面试被拒了，好难过";
        private BigDecimal emotionScoreAvg = BigDecimal.ZERO;
        private Integer aiResponseCount = 0;
        private Integer likeCount = 0;
        private Integer viewCount = 0;
        private Integer status = 1;

        public PostBuilder withId(Long id) { this.id = id; return this; }
        public PostBuilder withUserId(Long userId) { this.userId = userId; return this; }
        public PostBuilder withContent(String content) { this.content = content; return this; }
        public PostBuilder withStatus(Integer status) { this.status = status; return this; }
        public PostBuilder withAiResponseCount(Integer count) { this.aiResponseCount = count; return this; }

        public Post build() {
            Post post = new Post();
            post.setId(id);
            post.setUserId(userId);
            post.setContent(content);
            post.setEmotionScoreAvg(emotionScoreAvg);
            post.setAiResponseCount(aiResponseCount);
            post.setLikeCount(likeCount);
            post.setViewCount(viewCount);
            post.setStatus(status);
            post.setCreatedAt(LocalDateTime.now());
            return post;
        }
    }

    public static class AiReplyBuilder {
        private Long id = 1L;
        private Long postId = 1L;
        private String aiProvider = "DeepSeek";
        private String perspective = "心情";
        private BigDecimal emotionScore = new BigDecimal("7.0");
        private String content = "抱抱你，被拒绝不是终点";
        private LocalDateTime createdAt = LocalDateTime.now();

        public AiReplyBuilder withPostId(Long postId) { this.postId = postId; return this; }
        public AiReplyBuilder withAiProvider(String aiProvider) { this.aiProvider = aiProvider; return this; }
        public AiReplyBuilder withPerspective(String perspective) { this.perspective = perspective; return this; }
        public AiReplyBuilder withContent(String content) { this.content = content; return this; }

        public AiReply build() {
            AiReply reply = new AiReply();
            reply.setId(id);
            reply.setPostId(postId);
            reply.setAiProvider(aiProvider);
            reply.setPerspective(perspective);
            reply.setEmotionScore(emotionScore);
            reply.setContent(content);
            reply.setCreatedAt(createdAt);
            return reply;
        }
    }
}
