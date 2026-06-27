package com.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 面试对话响应 DTO。
 * 包含下一个问题、追问标志、或面试结束时的综合评价与参考答案。
 */
public class ChatResponse {

    private String sessionId;

    /** 下一个问题文本 */
    private String question;

    /** 题型：技术 / 项目 / 行为 */
    private String type;

    private Boolean isFollowUp;

    /** 是否已完成面试 */
    private Boolean isComplete;

    /** 综合评价总结 */
    private String summary;

    /** 总分（百分制，仅面试结束时有效） */
    private Integer totalScore;

    /** 优势列表 */
    private List<String> strengths;

    /** 待改进列表 */
    private List<String> weaknesses;

    private List<ModelAnswer> modelAnswers;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelAnswer {
        private String question;
        private String type;
        private Boolean isFollowUp;
        private String modelAnswer;
}
}
