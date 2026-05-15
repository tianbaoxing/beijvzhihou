package com.beijvzhihou.service;

import com.beijvzhihou.dto.CommentCreateDTO;
import com.beijvzhihou.dto.CommentVO;
import com.beijvzhihou.dto.PageResult;

public interface CommentService {
    CommentVO createComment(Long userId, Long postId, CommentCreateDTO dto);
    PageResult<CommentVO> listComments(Long postId, int page, int size);
}
