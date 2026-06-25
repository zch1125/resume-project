package com.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.Resume;
import com.resume.mapper.ResumeMapper;
import com.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final Tika tika = new Tika();

    @Override
    @Transactional
    public Resume uploadResume(MultipartFile file, String sessionId) {
        try {
            String rawText = tika.parseToString(file.getInputStream());
            Resume resume = new Resume();
            resume.setSessionId(sessionId);
            resume.setFileName(file.getOriginalFilename());
            resume.setRawText(rawText);
            resumeMapper.insert(resume);
            log.info("Resume uploaded: id={}, fileName={}", resume.getId(), resume.getFileName());
            return resume;
        } catch (Exception e) {
            log.error("Failed to upload resume", e);
            throw new RuntimeException("简历文件解析失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Resume parseResume(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在: id=" + resumeId);
        }
        String rawText = resume.getRawText();
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("简历文本为空，无法解析");
        }

        // 先尝试 AI 解析
        try {
            long start = System.currentTimeMillis();
            String prompt = "你是一位HR专家，请从以下简历文本中提取关键字段，以JSON格式返回（不要包含其他文字）：\n"
                + "{\n"
                + "    \"name\": \"姓名\",\n"
                + "    \"education\": \"学历\",\n"
                + "    \"workYears\": \"工作年限\",\n"
                + "    \"skills\": [\"技能1\", \"技能2\"],\n"
                + "    \"projects\": [\"项目经历1\", \"项目经历2\"],\n"
                + "    \"expectedPosition\": \"期望岗位\"\n"
                + "}\n\n"
                + "简历文本：\n" + rawText;

            String aiResponse = chatClient.prompt()
                    .system("你是一位专业的HR专家，擅长从简历中提取结构化信息。")
                    .user(prompt)
                    .call()
                    .content();
            long elapsed = System.currentTimeMillis() - start;
            log.info("AI parse for resumeId={}, cost={}ms", resumeId, elapsed);

            String json = cleanJsonResponse(aiResponse);
            objectMapper.readValue(json, Map.class);
            resume.setParsedJson(json);
            resumeMapper.updateById(resume);
            return resume;
        } catch (Exception e) {
            log.warn("AI parse failed for resumeId={}, using rule-based fallback: {}", resumeId, e.getMessage());
        }

        // 规则解析兜底
        String fallbackJson = ruleBasedParse(rawText);
        resume.setParsedJson(fallbackJson);
        resumeMapper.updateById(resume);
        return resume;
    }

    private String ruleBasedParse(String text) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("name", extractByPattern(text, "(?:姓[\\s\\u00A0]*名[：:＝\\s]*)([\\u4e00-\\u9fa5]{2,4})"));
            result.put("expectedPosition", extractByPattern(text, "(?:求职岗位|期望岗位|目标岗位|应聘[：:])\\s*([^\\n]{2,20})"));
            String edu = extractByPattern(text, "(本科|硕士|博士|专科|大专|研究生)");
            result.put("education", edu.isEmpty() ? "未识别" : edu);
            String years = extractByPattern(text, "(\\d+)\\s*年");
            result.put("workYears", years.isEmpty() ? "未识别" : years + "年");
            result.put("phone", extractByPattern(text, "(1[3-9]\\d{9})"));
            result.put("email", extractByPattern(text, "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"));

            List<String> skills = new ArrayList<>();
            for (String kw : "Java,Spring Boot,Spring Cloud,Vue,MySQL,Redis,MyBatis,Docker,Kubernetes,Linux,Git,Maven,Python,JavaScript,TypeScript,Node.js,AI,微服务,全栈".split(",")) {
                if (text.toLowerCase().contains(kw.toLowerCase())) skills.add(kw);
            }
            result.put("skills", skills.isEmpty() ? List.of("未识别") : skills);

            List<String> projects = new ArrayList<>();
            for (String line : text.split("\\n")) {
                line = line.trim();
                if (line.contains("项目") && line.length() > 4 && line.length() < 60) {
                    projects.add(line);
                    if (projects.size() >= 5) break;
                }
            }
            result.put("projects", projects.isEmpty() ? List.of("未识别") : projects.stream().distinct().collect(Collectors.toList()));

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Rule-based parse failed", e);
            return "{\"error\":\"简历解析失败\"}";
        }
    }

    private String extractByPattern(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    @Override
    public Resume getById(Long id) {
        Resume r = resumeMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("简历不存在: id=" + id);
        return r;
    }

    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        String cleaned = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        return (start >= 0 && end > start) ? cleaned.substring(start, end + 1) : "{}";
    }
}
