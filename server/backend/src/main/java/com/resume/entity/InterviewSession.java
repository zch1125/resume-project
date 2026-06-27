package com.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_session")
public class InterviewSession {

    /** 会话 ID（自增主键） */

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联简历 ID */
    private Long resumeId;

    /** 岗位招聘信息 */
    private String jobDescription;

    /** 对话历史（JSON 数组，包含所有问答轮次） */
    private String conversationHistory;

    /** 会话状态：in_progress={进行中}, completed={已结束} */
    private String status;

    /** 面试总分 */
    private Integer totalScore;

    /** 综合评价 */
    private String summary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
