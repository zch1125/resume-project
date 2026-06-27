package com.resume.service;

import com.resume.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    Resume uploadResume(MultipartFile file, String sessionId, Long userId);

    Resume parseResume(Long resumeId);

    Resume getById(Long id);
}