package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT COALESCE(SUM(emotion_score_avg), 0) / COUNT(*) FROM post WHERE user_id = #{userId} AND status = 1")
    Double selectAvgEmotionScoreByUserId(@Param("userId") Long userId);

    @Update("UPDATE post SET like_count = (SELECT COUNT(*) FROM post_like WHERE post_id = #{postId}) WHERE id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{postId}")
    int incrViewCount(@Param("postId") Long postId);
}