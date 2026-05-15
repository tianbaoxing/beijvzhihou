package com.beijvzhihou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.beijvzhihou.common.BusinessException;
import com.beijvzhihou.common.ResultCode;
import com.beijvzhihou.entity.Post;
import com.beijvzhihou.mapper.PostMapper;
import com.beijvzhihou.service.ReviewService;
import com.beijvzhihou.util.KeywordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired private PostMapper postMapper;
    @Autowired private KeywordFilter keywordFilter;

    @Override
    public boolean keywordFilter(String content) {
        return !keywordFilter.containsKeyword(content);
    }

    @Override
    public List<Post> getPendingPosts() {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 2) // 2=待审核
                .orderByAsc(Post::getCreatedAt);
        return postMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void reviewPost(Long postId, String result) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        if ("pass".equals(result)) {
            post.setStatus(1); // 1=已发布
        } else if ("reject".equals(result)) {
            post.setStatus(0); // 0=已拒绝
        } else {
            throw new BusinessException(ResultCode.FAIL);
        }
        postMapper.updateById(post);
        log.info("帖子 {} 审核结果：{}", postId, result);
    }
}