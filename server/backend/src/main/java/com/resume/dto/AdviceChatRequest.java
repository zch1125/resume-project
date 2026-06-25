package com.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdviceChatRequest {

    @NotNull(message = "建议ID不能为空")
    private Long suggestionId;

    @NotBlank(message = "消息内容不能为空")
    private String userMessage;
}
