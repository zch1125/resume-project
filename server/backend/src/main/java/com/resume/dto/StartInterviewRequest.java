package com.resume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartInterviewRequest {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    /** 岗位招聘信息文字（可选，与 imageData 至少提供一个） */
    private String jobDescription;

    /** 图片 base64 数据（可选，岗位截图） */
    private String imageData;

    /** 图片 MIME 类型，如 image/png（仅 imageData 有值时有效） */
    private String imageType;
}
