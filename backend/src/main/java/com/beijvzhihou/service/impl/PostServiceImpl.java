package com.beijvzhihou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.dto.AiReplyResult;
import com.beijvzhihou.dto.AiReplyVO;
import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.dto.PostCreateDTO;
import com.beijvzhihou.dto.PostVO;
import com.beijvzhihou.entity.AiReply;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.entity.PostLike;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.AiReplyMapper;
import com.beijvzhihou.mapper.PostLikeMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.AiStreamService;
import com.beijvzhihou.service.AIService;
import com.beijvzhihou.service.PostService;
import com.beijvzhihou.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostServiceImpl implements PostService {

    @Autowired private PostMapper postMapper;
    @Autowired private PostLikeMapper postLikeMapper;
    @Autowired private AiReplyMapper aiReplyMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ReviewService reviewService;
    @Autowired private AIService aiService;
    @Autowired private AiStreamService aiStreamService;
    @Autowired private com.beijvzhihou.util.KeywordFilter keywordFilter;

    @Override
    @Transactional
    public PostVO createPost(Long userId, PostCreateDTO dto) {
        // 关键词过滤
        if (!reviewService.keywordFilter(dto.getContent())) {
            String keyword = keywordFilter.findFirstKeyword(dto.getContent());
            throw new BusinessException(ResultCode.FORBIDDEN, "内容包含违禁词: " + keyword);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(dto.getContent());
        post.setEmotionScoreAvg(BigDecimal.ZERO);
        post.setAiResponseCount(0);
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setStatus(1);
        postMapper.insert(post);

        // 调用 AI 生成回复
        List<AiReply> aiReplies = new ArrayList<>();
        List<AiReplyResult> aiResults = aiService.generateReplies(dto.getContent());

        for (AiReplyResult result : aiResults) {
            AiReply reply = new AiReply();
            reply.setPostId(post.getId());
            reply.setAiProvider(result.getAiProvider());
            reply.setPerspective(result.getPerspective());
            reply.setEmotionScore(result.getEmotionScore());
            reply.setContent(result.getContent());
            reply.setCreatedAt(LocalDateTime.now());
            aiReplyMapper.insert(reply);
            aiReplies.add(reply);
        }

        post.setAiResponseCount(aiReplies.size());
        BigDecimal avg = aiReplies.stream()
                .map(AiReply::getEmotionScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(aiReplies.size()), 1, java.math.RoundingMode.HALF_UP);
        post.setEmotionScoreAvg(avg);
        postMapper.updateById(post);

        return toPostVO(post, null, null);
    }

    @Override
    @Transactional
    public PostVO createPostAndStartAiStream(Long userId, PostCreateDTO dto) {
        if (!reviewService.keywordFilter(dto.getContent())) {
            String keyword = keywordFilter.findFirstKeyword(dto.getContent());
            throw new BusinessException(ResultCode.FORBIDDEN, "内容包含违禁词: " + keyword);
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(dto.getContent());
        post.setEmotionScoreAvg(BigDecimal.ZERO);
        post.setAiResponseCount(0);
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setStatus(1);
        postMapper.insert(post);

        aiStreamService.startAiStream(post.getId(), dto.getContent());

        return toPostVO(post, null, null);
    }

    @Override
    public PageResult<PostVO> listPosts(int page, int size, String sort) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getCreatedAt);
        } else {
            wrapper.orderByDesc(Post::getCreatedAt);
        }
        Page<Post> postPage = postMapper.selectPage(pageParam, wrapper);
        List<PostVO> voList = postPage.getRecords().stream()
                .map(p -> toPostVO(p, null, null))
                .collect(Collectors.toList());
        return PageResult.of(voList, postPage.getTotal(), (int) postPage.getCurrent(), (int) postPage.getSize());
    }

    @Override
    public PageResult<PostVO> listUserPosts(Long userId, int page, int size) {
        Page<Post> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);
        Page<Post> postPage = postMapper.selectPage(pageParam, wrapper);
        List<PostVO> voList = postPage.getRecords().stream()
                .map(p -> toPostVO(p, null, null))
                .collect(Collectors.toList());
        return PageResult.of(voList, postPage.getTotal(), (int) postPage.getCurrent(), (int) postPage.getSize());
    }

    @Override
    @Transactional
    public PostVO getPost(Long postId, Long currentUserId, String fingerprint) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        postMapper.incrViewCount(postId);
        return toPostVO(post, currentUserId, fingerprint);
    }

    @Override
    @Transactional
    public void toggleLike(Long postId, Long userId, String fingerprint) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        boolean exists;
        if (userId != null) {
            exists = postLikeMapper.existsByPostIdAndUserId(postId, userId);
        } else {
            LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostLike::getPostId, postId)
                    .eq(PostLike::getIpHash, fingerprint)
                    .isNull(PostLike::getUserId);
            exists = postLikeMapper.selectCount(wrapper).intValue() > 0;
        }

        if (exists) {
            LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PostLike::getPostId, postId);
            if (userId != null) {
                wrapper.eq(PostLike::getUserId, userId);
            } else {
                wrapper.eq(PostLike::getIpHash, fingerprint).isNull(PostLike::getUserId);
            }
            postLikeMapper.delete(wrapper);
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setIpHash(userId == null ? fingerprint : null);
            postLikeMapper.insert(like);
        }
        postMapper.updateLikeCount(postId);
    }

    private PostVO toPostVO(Post post, Long currentUserId, String fingerprint) {
        User author = userMapper.selectById(post.getUserId());
        LambdaQueryWrapper<AiReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(AiReply::getPostId, post.getId()).orderByAsc(AiReply::getCreatedAt);
        List<AiReply> replies = aiReplyMapper.selectList(replyWrapper);
        List<AiReplyVO> replyVOList = replies.stream()
                .map(r -> new AiReplyVO(r.getId(), r.getAiProvider(), r.getPerspective(),
                        r.getEmotionScore(), r.getContent(), r.getCreatedAt()))
                .collect(Collectors.toList());

        Boolean liked = null;
        if (currentUserId != null) {
            liked = postLikeMapper.existsByPostIdAndUserId(post.getId(), currentUserId);
        } else if (fingerprint != null && !fingerprint.isEmpty()) {
            LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(PostLike::getPostId, post.getId())
                    .eq(PostLike::getIpHash, fingerprint)
                    .isNull(PostLike::getUserId);
            liked = postLikeMapper.selectCount(likeWrapper).intValue() > 0;
        }

        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setNickname(author != null ? author.getNickname() : "匿名用户");
        vo.setAvatarUrl(author != null ? author.getAvatarUrl() : "");
        vo.setContent(post.getContent());
        vo.setEmotionScoreAvg(post.getEmotionScoreAvg());
        vo.setLikeCount(post.getLikeCount());
        vo.setViewCount(post.getViewCount());
        vo.setAiResponseCount(post.getAiResponseCount());
        vo.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0);
        vo.setLiked(liked);
        vo.setCreatedAt(post.getCreatedAt());
        vo.setAiReplies(replyVOList);
        return vo;
    }
}