package com.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    private List<ScoreItem> scores;
    private int totalScore;
    private boolean passed;
    private boolean excellent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreItem {
        private Long questionId;
        private int score;
        private String reason;
    }
}
