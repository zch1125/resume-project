package com.resume.controller;

import com.resume.common.Result;
import com.resume.dto.ChatRequest;
import com.resume.dto.ChatResponse;
import com.resume.dto.StartInterviewRequest;
import com.resume.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    public Result<ChatResponse> start(@Valid @RequestBody StartInterviewRequest request) {
        ChatResponse response = interviewService.startInterview(request.getResumeId(), request.getJobDescription(), request.getImageData(), request.getImageType());
        return Result.success(response);
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = interviewService.chat(request.getSessionId(), request.getUserAnswer());
        return Result.success(response);
    }
}
