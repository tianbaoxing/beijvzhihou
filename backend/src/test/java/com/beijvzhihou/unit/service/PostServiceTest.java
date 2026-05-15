package com.beijvzhihou.unit.service;

import com.beijvzhihou.dto.PostVO;
import com.beijvzhihou.entity.AiReply;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.PostLikeMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.AIService;
import com.beijvzhihou.service.ReviewService;
import com.beijvzhihou.service.impl.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private AiReplyMapper aiReplyMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ReviewService reviewService;

    @Mock
    private AIService aiService;

    @InjectMocks
    private PostServiceImpl postService;

    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("帖子详情测试")
    class GetPostTest {

        @Test
        @DisplayName("获取帖子详情成功")
        void getPost_validId_returnsPostVO() {
            Post post = createTestPost();
            when(postMapper.selectById(POST_ID)).thenReturn(post);
            when(userMapper.selectById(USER_ID)).thenReturn(createTestUser());
            when(aiReplyMapper.selectList(any())).thenReturn(Collections.emptyList());

            PostVO result = postService.getPost(POST_ID, null, null);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(POST_ID);
        }
    }

    private Post createTestPost() {
        Post post = new Post();
        post.setId(POST_ID);
        post.setUserId(USER_ID);
        post.setContent("今天面试被拒了，好难过");
        post.setAiResponseCount(3);
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setStatus(1);
        return post;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setNickname("测试用户");
        user.setAvatarUrl("https://picsum.photos/seed/123");
        return user;
    }
}
