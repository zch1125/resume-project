package com.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("suggestion")
public class Suggestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Boolean needModify;

    private String suggestionsJson;

    private String overallComment;

    private String conversationHistory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
