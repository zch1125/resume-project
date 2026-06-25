package com.resume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ChatResponse;
import com.resume.entity.InterviewSession;
import com.resume.entity.Resume;
import com.resume.mapper.InterviewSessionMapper;
import com.resume.service.InterviewService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final ResumeService resumeService;
    private final InterviewSessionMapper sessionMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChatResponse startInterview(Long resumeId, String jobDescription) {
        Resume resume = resumeService.getById(resumeId);

        InterviewSession session = new InterviewSession();
        session.setResumeId(resumeId);
        session.setJobDescription(jobDescription);
        session.setStatus("in_progress");
        session.setConversationHistory("[]");
        sessionMapper.insert(session);

        try {
            long start = System.currentTimeMillis();
            String resumeText = resume.getRawText() != null ? resume.getRawText() : "";

            String prompt = """
                    你是一位专业的技术面试官。请根据候选人的简历和岗位招聘信息，开始面试。

                    岗位招聘信息：
                    %s

                    简历内容：
                    %s

                    规则：
                    1. 请先进行简单的问候开场，然后提出第一个面试问题
                    2. 一次只问一个问题
                    3. 面试问题应覆盖技术、项目经验和行为方面
                    4. 如果是高级岗位（资深、架构师、Tech Lead等），问题应该有更深的技术深度
                    5. 如果简历中提到了具体的项目或技术栈，优先针对这些内容提问

                    请严格按以下 JSON 格式返回（不要包含其他文字）：
                    {
                        "question": "第一个面试问题文本",
                        "type": "技术" 或 "项目" 或 "行为",
                        "isFollowUp": false
                    }
                    """.formatted(jobDescription, resumeText);

            String aiResponse = chatClient.prompt()
                    .system("你是一位资深技术面试官，擅长根据简历和岗位信息进行模拟面试。请一次只问一个问题，真实模拟面试场景。")
                    .user(prompt)
                    .call()
                    .content();

            long elapsed = System.currentTimeMillis() - start;
            log.info("Interview start: resumeId={}, sessionId={}, cost={}ms", resumeId, session.getId(), elapsed);

            Map<String, Object> questionData = parseJsonResponse(aiResponse);
            String question = (String) questionData.getOrDefault("question", "");
            String type = (String) questionData.getOrDefault("type", "技术");

            List<Map<String, Object>> history = new ArrayList<>();
            Map<String, Object> aiTurn = new HashMap<>();
            aiTurn.put("role", "ai");
            aiTurn.put("question", question);
            aiTurn.put("type", type);
            aiTurn.put("isFollowUp", false);
            history.add(aiTurn);
            session.setConversationHistory(objectMapper.writeValueAsString(history));
            sessionMapper.updateById(session);

            return ChatResponse.builder()
                    .question(question)
                    .type(type)
                    .sessionId(String.valueOf(session.getId()))
                    .isFollowUp(false)
                    .isComplete(false)
                    .build();
        } catch (Exception e) {
            log.error("Failed to start interview for resumeId={}", resumeId, e);
            throw new RuntimeException("面试启动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ChatResponse chat(String sessionIdStr, String userAnswer) {
        Long sessionId = Long.valueOf(sessionIdStr);
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("面试会话不存在");
        }
        if ("completed".equals(session.getStatus())) {
            throw new RuntimeException("面试已结束");
        }

        Resume resume = resumeService.getById(session.getResumeId());

        try {
            long start = System.currentTimeMillis();

            List<Map<String, Object>> history = parseHistory(session.getConversationHistory());
            Map<String, Object> userTurn = new HashMap<>();
            userTurn.put("role", "user");
            userTurn.put("answer", userAnswer);
            history.add(userTurn);

            StringBuilder historyText = new StringBuilder();
            for (int i = 0; i < history.size(); i++) {
                Map<String, Object> turn = history.get(i);
                String role = (String) turn.get("role");
                if ("ai".equals(role)) {
                    String q = (String) turn.getOrDefault("question", "");
                    String t = (String) turn.getOrDefault("type", "技术");
                    boolean isFU = Boolean.TRUE.equals(turn.get("isFollowUp"));
                    historyText.append("面试官").append(" (").append(t).append(")");
                    if (isFU) historyText.append(" [追问]");
                    historyText.append("：").append(q).append("\n\n");
                } else {
                    String a = (String) turn.getOrDefault("answer", "");
                    historyText.append("候选人：").append(a).append("\n\n");
                }
            }

            String resumeText = resume.getRawText() != null ? resume.getRawText() : "";
            String jobDescription = session.getJobDescription();

            String prompt = """
                    你是一位专业的技术面试官。请继续以下面试，根据对话历史和候选人的最新回答，决定下一步动作。

                    岗位招聘信息：
                    %s

                    简历内容：
                    %s

                    对话历史：
                    %s

                    规则：
                    1. 仔细分析候选人的最新回答的质量和深度
                    2. 根据岗位级别决定动作：
                       - 高级岗位（资深、架构师、Tech Lead、专家等）：如果回答较浅，追问深挖2-3次再换话题
                       - 中级岗位：可追问1次或直接换话题
                       - 初级岗位：直接换话题为主，少追问
                    3. 当某个话题已充分考察，切换到新的话题
                    4. 一般覆盖5-8轮对话后可以考虑结束面试，高级岗位可更多
                    5. 确保覆盖技术、项目、行为等多个方面

                    请严格按以下 JSON 格式返回（不要包含其他文字）：
                    {
                        "action": "continue" 或 "complete",
                        "question": "下一个问题（仅当action为continue）",
                        "type": "技术" 或 "项目" 或 "行为"（仅当action为continue）,
                        "isFollowUp": true 或 false（仅当action为continue，true表示追问当前话题）,
                        "summary": "综合评价（仅当action为complete），包括面试总结、技术能力评估、项目经验评估、沟通能力等",
                        "totalScore": 85（仅当action为complete，百分制）,
                        "strengths": ["优势1", "优势2", "优势3"]（仅当action为complete）,
                        "weaknesses": ["不足1", "不足2"]（仅当action为complete）
                    }
                    """.formatted(jobDescription, resumeText, historyText.toString());

            String aiResponse = chatClient.prompt()
                    .system("你是一位资深技术面试官，擅长根据候选人的回答动态调整面试策略，进行真实模拟面试。")
                    .user(prompt)
                    .call()
                    .content();

            long elapsed = System.currentTimeMillis() - start;
            log.info("Interview chat: sessionId={}, cost={}ms", sessionId, elapsed);

            Map<String, Object> responseData = parseJsonResponse(aiResponse);
            String action = (String) responseData.get("action");

            if ("complete".equals(action)) {
                session.setStatus("completed");
                session.setConversationHistory(objectMapper.writeValueAsString(history));
                sessionMapper.updateById(session);

                String summary = (String) responseData.getOrDefault("summary", "");
                Integer totalScore = responseData.get("totalScore") != null
                        ? Integer.parseInt(responseData.get("totalScore").toString())
                        : 0;

                @SuppressWarnings("unchecked")
                List<String> strengths = responseData.get("strengths") != null
                        ? (List<String>) responseData.get("strengths")
                        : new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<String> weaknesses = responseData.get("weaknesses") != null
                        ? (List<String>) responseData.get("weaknesses")
                        : new ArrayList<>();

                return ChatResponse.builder()
                        .isComplete(true)
                        .summary(summary)
                        .totalScore(totalScore)
                        .strengths(strengths)
                        .weaknesses(weaknesses)
                        .build();
            } else {
                String question = (String) responseData.getOrDefault("question", "");
                String type = (String) responseData.getOrDefault("type", "技术");
                boolean isFollowUp = Boolean.TRUE.equals(responseData.get("isFollowUp"));

                Map<String, Object> aiTurn = new HashMap<>();
                aiTurn.put("role", "ai");
                aiTurn.put("question", question);
                aiTurn.put("type", type);
                aiTurn.put("isFollowUp", isFollowUp);
                history.add(aiTurn);

                session.setConversationHistory(objectMapper.writeValueAsString(history));
                sessionMapper.updateById(session);

                return ChatResponse.builder()
                        .question(question)
                        .type(type)
                        .sessionId(sessionIdStr)
                        .isFollowUp(isFollowUp)
                        .isComplete(false)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to process chat for sessionId={}", sessionId, e);
            throw new RuntimeException("面试处理失败：" + e.getMessage());
        }
    }

    private Map<String, Object> parseJsonResponse(String aiResponse) {
        try {
            String json = cleanJsonResponse(aiResponse);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response: {}", aiResponse, e);
            throw new RuntimeException("AI返回格式解析失败");
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

