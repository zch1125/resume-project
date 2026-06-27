package com.resume.controller;

import com.resume.common.Result;
import com.resume.dto.ChatRequest;
import com.resume.dto.InterviewHistoryItem;
import java.util.List;
import com.resume.dto.ChatResponse;
import com.resume.dto.StartInterviewRequest;
import com.resume.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 面试相关 API。
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 启动新的面试会话。
     * 接收简历 ID、岗位信息（文字 + 可选图片），返回第一个问题。
     */
    @PostMapping("/start")
    public Result<ChatResponse> start(@Valid @RequestBody StartInterviewRequest request) {
        ChatResponse response = interviewService.startInterview(request.getResumeId(), request.getJobDescription(), request.getImageData(), request.getImageType());
        return Result.success(response);
    }

    /**
     * 用户回答当前问题并获取下一步响应。
     * 可能返回下一个问题（追问或换题），或面试结束的综合评价。
     */
    /**
     * 查询用户的面试历史记录。
     * 通过前端 session ID 查询该用户所有的面试会话。
     */
    @GetMapping("/history")
    public Result<List<InterviewHistoryItem>> history(@RequestParam String sessionId) {
        List<InterviewHistoryItem> items = interviewService.getInterviewHistory(sessionId);
        return Result.success(items);
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = interviewService.chat(request.getSessionId(), request.getUserAnswer());
        return Result.success(response);
    }
}
