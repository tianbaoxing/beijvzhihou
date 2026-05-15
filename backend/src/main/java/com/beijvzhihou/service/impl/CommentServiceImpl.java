package com.beijvzhihou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.dto.CommentCreateDTO;
import com.beijvzhihou.dto.CommentVO;
import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.entity.Comment;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.entity.User;
import com.beijvzhihou.mapper.CommentMapper;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.mapper.UserMapper;
import com.beijvzhihou.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired private CommentMapper commentMapper;
    @Autowired private PostMapper postMapper;
    @Autowired private UserMapper userMapper;

    @Override
    @Transactional
    public CommentVO createComment(Long userId, Long postId, CommentCreateDTO dto) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        if (dto.getParentId() != null) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null || !parent.getPostId().equals(postId)) {
                throw new BusinessException(ResultCode.FAIL, "父评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        commentMapper.updateCommentCount(postId);

        log.info("用户 {} 评论帖子 {} 成功, commentId={}", userId, postId, comment.getId());

        return toCommentVO(comment);
    }

    @Override
    public PageResult<CommentVO> listComments(Long postId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt);
        Page<Comment> commentPage = commentMapper.selectPage(pageParam, wrapper);

        List<CommentVO> voList = commentPage.getRecords().stream()
                .map(this::toCommentVO)
                .collect(Collectors.toList());

        List<Long> parentIds = voList.stream()
                .map(CommentVO::getId)
                .collect(Collectors.toList());

        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getPostId, postId)
                    .in(Comment::getParentId, parentIds)
                    .orderByAsc(Comment::getCreatedAt);
            List<Comment> replies = commentMapper.selectList(replyWrapper);

            Map<Long, List<CommentVO>> replyMap = replies.stream()
                    .map(this::toCommentVO)
                    .collect(Collectors.groupingBy(CommentVO::getParentId));

            for (CommentVO vo : voList) {
                vo.setReplies(replyMap.getOrDefault(vo.getId(), new ArrayList<>()));
            }
        }

        return PageResult.of(voList, commentPage.getTotal(), (int) commentPage.getCurrent(), (int) commentPage.getSize());
    }

    private CommentVO toCommentVO(Comment comment) {
        User user = userMapper.selectById(comment.getUserId());
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setNickname(user != null ? user.getNickname() : "匿名用户");
        vo.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplies(new ArrayList<>());
        return vo;
    }
}
