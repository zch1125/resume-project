package com.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("answer_record")
public class AnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private String userAnswer;

    private Integer score;

    private String scoreReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
