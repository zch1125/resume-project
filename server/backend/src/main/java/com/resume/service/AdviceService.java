package com.resume.service;

import com.resume.dto.AdviceResponse;

public interface AdviceService {

    AdviceResponse generateAdvice(Long resumeId, String jobTitle);

    AdviceResponse chatAdvice(Long suggestionId, String userMessage);
}
