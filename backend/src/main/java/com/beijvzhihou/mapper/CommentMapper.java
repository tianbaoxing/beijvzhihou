package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE post SET comment_count = (SELECT COUNT(*) FROM comment WHERE post_id = #{postId}) WHERE id = #{postId}")
    int updateCommentCount(@Param("postId") Long postId);
}
