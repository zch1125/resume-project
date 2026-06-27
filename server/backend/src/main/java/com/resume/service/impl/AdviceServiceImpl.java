package com.resume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.AdviceResponse;
import com.resume.entity.Resume;
import com.resume.entity.Suggestion;
import com.resume.mapper.SuggestionMapper;
import com.resume.service.AdviceService;
import com.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.resume.util.AiRetryUtil;

@Slf4j
/**
 * 简历修改建议服务实现类。
 * 支持一次性生成建议与多轮对话优化。
 */
@Service
@RequiredArgsConstructor
public class AdviceServiceImpl implements AdviceService {

    private final ResumeService resumeService;
    private final SuggestionMapper suggestionMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * 生成初始简历修改建议。
     * 根据简历内容和目标岗位，调用 AI 进行评估并持久化结果。
     */
    @Override
    @Transactional
    public AdviceResponse generateAdvice(Long resumeId, String jobTitle) {
        Resume resume = resumeService.getById(resumeId);
        resume.setJobTitle(jobTitle);

        try {
            long start = System.currentTimeMillis();

            String prompt = """
                    你是一位资深简历优化师。请根据以下简历内容和目标岗位，对简历进行评估。
                    目标岗位：%s

                    简历全文：
                    %s

                    请严格按照以下JSON格式返回（不要包含其他文字）：
                    {
                        "needModify": true/false,
                        "suggestions": [
                            {
                                "section": "需要修改的部分名称",
                                "original": "原文",
                                "suggested": "修改建议"
                            }
                        ],
                        "overallComment": "整体评价"
                    }

                    注意：如果简历已经很好，将 needModify 设为 false，suggestions 设为空数组。
                    """.formatted(jobTitle, resume.getRawText());

            String aiResponse = AiRetryUtil.callWithRetry(() ->
                    chatClient.prompt()
                    .system("你是一位资深简历优化师，擅长分析简历并给出有针对性的修改建议。")
                    .user(prompt)
                    .call()
                    .content(),

                    "generateAdvice"
            );

            long elapsed = System.currentTimeMillis() - start;
            log.info("AI advice generated for resumeId={}, cost={}ms", resumeId, elapsed);

            AdviceResponse adviceResponse = parseAdviceResponse(aiResponse);

            // Persist
            Suggestion suggestion = new Suggestion();
            suggestion.setResumeId(resumeId);
            suggestion.setNeedModify(adviceResponse.isNeedModify());
            suggestion.setSuggestionsJson(objectMapper.writeValueAsString(adviceResponse.getSuggestions()));
            suggestion.setOverallComment(adviceResponse.getOverallComment());
            suggestionMapper.insert(suggestion);

            adviceResponse.setSuggestionId(suggestion.getId());

            return adviceResponse;
        } catch (Exception e) {
            log.error("Failed to generate advice for resumeId={}", resumeId, e);
            AdviceResponse fallback = new AdviceResponse();
            fallback.setNeedModify(false);
            fallback.setOverallComment("AI建议生成失败，请稍后重试");
            return fallback;
        }
    }

    /**
     * 对建议进行多轮对话优化。
     * 根据用户反馈调用 AI 优化建议内容，并更新持久化记录。
     */
    @Override
    @Transactional
    public AdviceResponse chatAdvice(Long suggestionId, String userMessage) {
        Suggestion suggestion = suggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            throw new RuntimeException("建议记录不存在");
        }

        Resume resume = resumeService.getById(suggestion.getResumeId());

        try {
            long start = System.currentTimeMillis();

            // Parse conversation history
            List<Map<String, Object>> history = parseHistory(suggestion.getConversationHistory());
            Map<String, Object> userTurn = new HashMap<>();
            userTurn.put("role", "user");
            userTurn.put("content", userMessage);
            history.add(userTurn);

            // Build history text for prompt
            StringBuilder historyText = new StringBuilder();
            for (Map<String, Object> turn : history) {
                String role = (String) turn.get("role");
                String content = (String) turn.getOrDefault("content", "");
                historyText.append("用户").append("：").append(content).append("\n\n");
            }

            String prompt = """
                    你是一位资深的简历优化师。以下是你之前给这份简历生成的修改建议：

                    当前建议：
                    %s

                    对话历史：
                    %s

                    用户的新消息：
                    %s

                    规则：
                    1. 根据用户的反馈，优化/改进相应的建议内容
                    2. 保持整体评价与新建议一致
                    3. 如果没有需要修改的，needModify 设为 false

                    请严格按以下 JSON 格式返回（不要包含其他文字）：
                    {
                        "needModify": true/false,
                        "suggestions": [
                            {
                                "section": "需要修改的部分名称",
                                "original": "原文",
                                "suggested": "修改建议"
                            }
                        ],
                        "overallComment": "整体评价"
                    }
                    """.formatted(
                            suggestion.getSuggestionsJson() != null ? suggestion.getSuggestionsJson() : "[]",
                            historyText.toString(),
                            userMessage);

            String aiResponse = AiRetryUtil.callWithRetry(() ->
                    chatClient.prompt()
                    .system("你是一位资深简历优化师，擅长根据用户反馈迭代优化简历建议。")
                    .user(prompt)
                    .call()
                    .content(),

                    "chatAdvice"
            );

            long elapsed = System.currentTimeMillis() - start;
            log.info("Advice chat: suggestionId={}, cost={}ms", suggestionId, elapsed);

            AdviceResponse adviceResponse = parseAdviceResponse(aiResponse);
            adviceResponse.setSuggestionId(suggestionId);

            // Save AI response to history
            Map<String, Object> aiTurn = new HashMap<>();
            aiTurn.put("role", "ai");
            aiTurn.put("content", objectMapper.writeValueAsString(adviceResponse));
            history.add(aiTurn);
            suggestion.setConversationHistory(objectMapper.writeValueAsString(history));
            suggestion.setSuggestionsJson(objectMapper.writeValueAsString(adviceResponse.getSuggestions()));
            suggestion.setOverallComment(adviceResponse.getOverallComment());
            suggestion.setNeedModify(adviceResponse.isNeedModify());
            suggestionMapper.updateById(suggestion);

            return adviceResponse;
        } catch (Exception e) {
            log.error("Failed to chat advice for suggestionId={}", suggestionId, e);
            throw new RuntimeException("建议对话处理失败：" + e.getMessage());
        }
    }

    private AdviceResponse parseAdviceResponse(String aiResponse) {
        try {
            String json = cleanJsonResponse(aiResponse);
            return objectMapper.readValue(json, AdviceResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI advice response, using fallback", e);
            AdviceResponse fallback = new AdviceResponse();
            fallback.setNeedModify(false);
            fallback.setOverallComment("AI建议解析失败，请稍后重试");
            return fallback;
        }
    }

    private List<Map<String, Object>> parseHistory(String historyJson) {
        try {
            if (historyJson == null || historyJson.isBlank() || "[]".equals(historyJson.trim())) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(historyJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse conversation history", e);
            return new ArrayList<>();
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        String cleaned = response.replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return "{}";
    }
}
