package com.resume.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AnswerRequest {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @Valid
    @Size(min = 1, max = 20, message = "答案数量应在1-20之间")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        @NotBlank(message = "用户答案不能为空")
        private String userAnswer;
    }
}
