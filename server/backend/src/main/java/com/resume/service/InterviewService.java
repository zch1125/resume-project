package com.resume.service;

import com.resume.dto.ChatResponse;
import com.resume.dto.StartInterviewRequest;


public interface InterviewService {

    ChatResponse startInterview(Long resumeId, String jobDescription, String imageData, String imageType);

    ChatResponse chat(String sessionId, String userAnswer);
}
