package com.beijvzhihou.unit.service;

import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.service.impl.ReviewServiceImpl;
import com.beijvzhihou.util.KeywordFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private KeywordFilter keywordFilter;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Nested
    @DisplayName("关键词过滤测试")
    class KeywordFilterTest {

        @Test
        @DisplayName("安全内容通过过滤")
        void keywordFilter_safeContent_returnsTrue() {
            when(keywordFilter.containsKeyword("今天面试被拒了")).thenReturn(false);

            boolean result = reviewService.keywordFilter("今天面试被拒了");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("违禁内容被拦截")
        void keywordFilter_forbiddenContent_returnsFalse() {
            when(keywordFilter.containsKeyword("违禁内容")).thenReturn(true);

            boolean result = reviewService.keywordFilter("违禁内容");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("帖子审核测试")
    class ReviewPostTest {

        @Test
        @DisplayName("审核通过：帖子状态改为1")
        void reviewPost_pass_setsStatus1() {
            Post post = new Post();
            post.setId(1L);
            post.setStatus(2);
            when(postMapper.selectById(1L)).thenReturn(post);

            reviewService.reviewPost(1L, "pass");

            assertThat(post.getStatus()).isEqualTo(1);
            verify(postMapper).updateById(post);
        }

        @Test
        @DisplayName("审核拒绝：帖子状态改为0")
        void reviewPost_reject_setsStatus0() {
            Post post = new Post();
            post.setId(1L);
            post.setStatus(2);
            when(postMapper.selectById(1L)).thenReturn(post);

            reviewService.reviewPost(1L, "reject");

            assertThat(post.getStatus()).isEqualTo(0);
            verify(postMapper).updateById(post);
        }

        @Test
        @DisplayName("帖子不存在时抛出异常")
        void reviewPost_notFound_throwsException() {
            when(postMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> reviewService.reviewPost(999L, "pass"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("无效审核结果抛出异常")
        void reviewPost_invalidResult_throwsException() {
            Post post = new Post();
            post.setId(1L);
            post.setStatus(2);
            when(postMapper.selectById(1L)).thenReturn(post);

            assertThatThrownBy(() -> reviewService.reviewPost(1L, "invalid"))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
