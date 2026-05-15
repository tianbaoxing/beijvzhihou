package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.AiReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface AiReplyMapper extends BaseMapper<AiReply> {

    @Select("SELECT AVG(trigger_score) FROM ai_reply WHERE post_id = #{postId}")
    BigDecimal selectAvgEmotionScore(Long postId);
}
