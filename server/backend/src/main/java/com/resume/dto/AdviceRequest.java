package com.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdviceRequest {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @NotBlank(message = "目标岗位不能为空")
    private String jobTitle;
}
