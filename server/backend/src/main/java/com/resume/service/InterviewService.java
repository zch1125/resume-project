package com.resume.service;

import com.resume.dto.ChatResponse;
import com.resume.dto.InterviewHistoryItem;
import java.util.List;
import com.resume.dto.StartInterviewRequest;


public interface InterviewService {

    ChatResponse startInterview(Long resumeId, String jobDescription, String imageData, String imageType);

    ChatResponse chat(String sessionId, String userAnswer);

    List<InterviewHistoryItem> getInterviewHistory(String frontendSessionId);
}
