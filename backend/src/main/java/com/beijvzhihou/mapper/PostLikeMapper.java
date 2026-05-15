package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}")
    int countByPostId(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) > 0 FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}