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
public class ChatResponse {

    private String sessionId;

    private String question;

    private String type;

    private Boolean isFollowUp;

    private Boolean isComplete;

    private String summary;

    private Integer totalScore;

    private List<String> strengths;

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
