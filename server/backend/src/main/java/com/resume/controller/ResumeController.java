package com.resume.controller;

import com.resume.common.Result;
import com.resume.dto.AdviceRequest;
import com.resume.dto.AdviceChatRequest;
import com.resume.dto.AdviceResponse;
import com.resume.entity.Resume;
import com.resume.service.AdviceService;
import com.resume.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final AdviceService adviceService;

    @PostMapping("/upload")
    public Result<Map<String, Long>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId) {
        Resume resume = resumeService.uploadResume(file, sessionId);
        return Result.success(Map.of("resumeId", resume.getId()));
    }

    @PostMapping("/parse/{id}")
    public Result<Resume> parse(@PathVariable Long id) {
        Resume resume = resumeService.parseResume(id);
        return Result.success(resume);
    }

    @PostMapping("/advice")
    public Result<AdviceResponse> advice(@Valid @RequestBody AdviceRequest request) {
        AdviceResponse response = adviceService.generateAdvice(request.getResumeId(), request.getJobTitle());
        return Result.success(response);
    }

    @PostMapping("/advice/chat")
    public Result<AdviceResponse> adviceChat(@Valid @RequestBody AdviceChatRequest request) {
        AdviceResponse response = adviceService.chatAdvice(request.getSuggestionId(), request.getUserMessage());
        return Result.success(response);
    }

    @GetMapping("/{id}")
    public Result<Resume> getById(@PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        return Result.success(resume);
    }
}