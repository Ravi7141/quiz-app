package com.example.quiz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TokenVerifyResponse {
    private String token;
    private Long examId;
    private String examType; // QUIZ or CODING
    private String studentEmail;
    private Long studentId;
    private String studentName;
    private String shareToken; // Added for unified assessment redirection
    
    // Exam Details to show on the entry screen
    private String examTitle;
    private String description;
    private Integer durationMinutes; // Only for Quiz
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private String difficulty;       // Only for Coding
}
