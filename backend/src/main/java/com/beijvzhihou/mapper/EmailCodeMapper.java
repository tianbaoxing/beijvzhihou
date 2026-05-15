package com.beijvzhihou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beijvzhihou.entity.EmailCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmailCodeMapper extends BaseMapper<EmailCode> {

    @Select("SELECT * FROM email_code WHERE email = #{email} AND type = #{type} AND used = 0 AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    EmailCode selectLatestValid(@Param("email") String email, @Param("type") String type);

    @Update("UPDATE email_code SET used = 1 WHERE id = #{id}")
    int markAsUsed(@Param("id") Long id);
}