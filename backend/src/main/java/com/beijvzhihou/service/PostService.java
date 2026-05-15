package com.beijvzhihou.service;

import com.beijvzhihou.dto.PageResult;
import com.beijvzhihou.dto.PostCreateDTO;
import com.beijvzhihou.dto.PostVO;

public interface PostService {
    PostVO createPost(Long userId, PostCreateDTO dto);
    PostVO createPostAndStartAiStream(Long userId, PostCreateDTO dto);
    PageResult<PostVO> listPosts(int page, int size, String sort);
    PostVO getPost(Long postId, Long currentUserId, String fingerprint);
    void toggleLike(Long postId, Long userId, String fingerprint);
}
