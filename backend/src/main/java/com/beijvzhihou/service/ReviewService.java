package com.beijvzhihou.service;

import com.beijvzhihou.entity.Post;
import java.util.List;

public interface ReviewService {
    /**
     * 关键词过滤
     * @param content 内容
     * @return true=安全，false=含违禁词
     */
    boolean keywordFilter(String content);

    /**
     * 获取待审核帖子列表
     */
    List<Post> getPendingPosts();

    /**
     * 审核帖子（通过/拒绝）
     * @param postId 帖子ID
     * @param result pass=通过 reject=拒绝
     */
    void reviewPost(Long postId, String result);
}