package com.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdviceResponse {

    private Long suggestionId;

    private boolean needModify;
    private List<SuggestionItem> suggestions;
    private String overallComment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestionItem {
        private String section;
        private String original;
        private String suggested;
    }
}
