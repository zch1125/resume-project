package com.resume.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resume.entity.InterviewSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 面试会话 Mapper，用于持久化与查询面试会话。
 */
@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSession> {
    /**
     * 根据前端 session ID 查询所有面试会话。
     * 通过 resume 表关联查询，按创建时间倒序。
     */
    @Select("SELECT s.id, s.resume_id, s.job_description, s.status, s.total_score, s.summary, s.create_time "
            + "FROM interview_session s JOIN resume r ON s.resume_id = r.id "
            + "WHERE r.session_id = #{sessionId} ORDER BY s.create_time DESC")
    List<Map<String, Object>> selectBySessionId(@Param("sessionId") String sessionId);
}
