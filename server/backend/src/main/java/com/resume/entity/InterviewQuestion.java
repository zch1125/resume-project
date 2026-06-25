package com.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_question")
public class InterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private String jobTitle;

    private String question;

    private String type;

    private String referenceAnswer;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
