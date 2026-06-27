package com.resume.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ChatResponse;
import com.resume.dto.InterviewHistoryItem;
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
import java.time.LocalDateTime;
import com.resume.util.AiRetryUtil;

@Slf4j
/**
 * 面试服务实现类。
 * 实现一问一答式对话面试，会话历史持久化到数据库，
 * AI 根据候选人回答动态决定追问、换题还是结束面试。
 */
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final ResumeService resumeService;
    private final InterviewSessionMapper sessionMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * 启动新的面试会话。
     * 1. 创建会话并持久化
     * 2. 构建 prompt（含简历 + 岗位信息 + 可选图片）
     * 3. 调用 AI 生成第一个问题
     * 4. 保存对话历史
     */
    @Override
    @Transactional
    public ChatResponse startInterview(Long resumeId, String jobDescription, String imageData, String imageType) {
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
                    0. 如果用户还上传了岗位招聘信息的图片，请结合图片和文字内容一起分析
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

            String fullPrompt = imageData != null && !imageData.isEmpty()
                    ? "![\u5c97\u4f4d\u62db\u8058\u4fe1\u606f](" + "data:" + imageType + ";base64," + imageData + ")\n\n" + prompt
                    : prompt;
            String aiResponse = AiRetryUtil.callWithRetry(() ->
                    chatClient.prompt()
                    .system("\u4f60\u662f\u4e00\u4f4d\u8d44\u6df1\u6280\u672f\u9762\u8bd5\u5b98\uff0c\u64c5\u957f\u6839\u636e\u7b80\u5386\u548c\u5c97\u4f4d\u4fe1\u606f\u8fdb\u884c\u6a21\u62df\u9762\u8bd5\u3002\u8bf7\u4e00\u6b21\u53ea\u95ee\u4e00\u4e2a\u95ee\u9898\uff0c\u771f\u5b9e\u6a21\u62df\u9762\u8bd5\u573a\u666f\u3002")
                    .user(fullPrompt)
                    .call()
                    .content(),

                    "startInterview"
            );

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

    /**
     * 处理用户回答并返回下一步响应。
     * 1. 获取会话并解析对话历史
     * 2. 追加用户回答到历史
     * 3. 构建 prompt 调用 AI 决定下一步动作
     * 4. 根据 AI 响应执行：
     *    - continue：追加新问题并返回
     *    - complete：更新会话状态为完成，返回综合评价和参考答案
     */
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
                    6. 结束时为每个问题提供“理想回答(modelAnswer)”，帮助候选人了解应该怎么回答，基于他们的简历和岗位要求

                    请严格按以下 JSON 格式返回（不要包含其他文字）：
                    {
                        "action": "continue" 或 "complete",
                        "question": "下一个问题（仅当action为continue）",
                        "type": "技术" 或 "项目" 或 "行为"（仅当action为continue）,
                        "isFollowUp": true 或 false（仅当action为continue，true表示追问当前话题）,
                        "summary": "综合评价（仅当action为complete），包括面试总结、技术能力评估、项目经验评估、沟通能力等",
                        "totalScore": 85（仅当action为complete，百分制）,
                        "strengths": ["优势1", "优势2", "优势3"]（仅当action为complete）,
                        "weaknesses": ["不足1", "不足2"]（仅当action为complete）,
                        "modelAnswers": [
                            {
                                "question": "问题文本",
                                "type": "技术" 或 "项目" 或 "行为",
                                "isFollowUp": true 或 false,
                                "modelAnswer": "基于候选人简历和岗位要求的理想回答"
                            }
                        ]
                    }
                    """.formatted(jobDescription, resumeText, historyText.toString());

            String aiResponse = AiRetryUtil.callWithRetry(() ->
                    chatClient.prompt()
                    .system("你是一位资深技术面试官，擅长根据候选人的回答动态调整面试策略，进行真实模拟面试。")
                    .user(prompt)
                    .call()
                    .content(),

                    "chat"
            );

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
                session.setTotalScore(totalScore);
                session.setSummary(summary);

                @SuppressWarnings("unchecked")
                List<String> strengths = responseData.get("strengths") != null
                        ? (List<String>) responseData.get("strengths")
                        : new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<String> weaknesses = responseData.get("weaknesses") != null
                        ? (List<String>) responseData.get("weaknesses")
                        : new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<ChatResponse.ModelAnswer> modelAnswers = new ArrayList<>();
                if (responseData.get("modelAnswers") != null) {
                    List<Map<String, Object>> rawModels = (List<Map<String, Object>>) responseData.get("modelAnswers");
                    for (Map<String, Object> m : rawModels) {
                        ChatResponse.ModelAnswer ma = ChatResponse.ModelAnswer.builder()
                                .question((String) m.getOrDefault("question", ""))
                                .type((String) m.getOrDefault("type", ""))
                                .isFollowUp(Boolean.TRUE.equals(m.get("isFollowUp")))
                                .modelAnswer((String) m.getOrDefault("modelAnswer", ""))
                                .build();
                        modelAnswers.add(ma);
                    }
                }
                return ChatResponse.builder()
                        .isComplete(true)
                        .summary(summary)
                        .totalScore(totalScore)
                        .strengths(strengths)
                        .weaknesses(weaknesses)
                        .modelAnswers(modelAnswers)
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


    @Override
    public List<InterviewHistoryItem> getInterviewHistory(String frontendSessionId) {
        List<Map<String, Object>> rows = sessionMapper.selectBySessionId(frontendSessionId);
        List<InterviewHistoryItem> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int questionCount = 0;
            String historyJson = (String) row.get("conversation_history");
            if (historyJson != null) {
                try {
                    List<Map<String, Object>> history = objectMapper.readValue(historyJson,
                            new TypeReference<List<Map<String, Object>>>() {});
                    for (Map<String, Object> turn : history) {
                        if ("ai".equals(turn.get("role")) && turn.containsKey("question")) {
                            questionCount++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse conversation history for session {}", row.get("id"));
                }
            }
            items.add(InterviewHistoryItem.builder()
                    .id(row.get("id") != null ? Long.valueOf(row.get("id").toString()) : null)
                    .resumeId(row.get("resume_id") != null ? Long.valueOf(row.get("resume_id").toString()) : null)
                    .jobDescription((String) row.get("job_description"))
                    .status((String) row.get("status"))
                    .totalScore(row.get("total_score") != null ? Integer.valueOf(row.get("total_score").toString()) : null)
                    .summary((String) row.get("summary"))
                    .questionCount(questionCount)
                    .createTime(row.get("create_time") != null
                            ? LocalDateTime.parse(row.get("create_time").toString().replace(" ", "T").substring(0, 19))
                            : null)
                    .build());
        }
        return items;
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

