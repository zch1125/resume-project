package com.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartInterviewRequest {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    @NotBlank(message = "岗位招聘信息不能为空")
    private String jobDescription;

    private String imageData;

    private String imageType;
}
