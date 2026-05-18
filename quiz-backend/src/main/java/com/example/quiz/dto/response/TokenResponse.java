package com.example.quiz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TokenResponse {
    private String token;
    private Long examId;
    private String examType;
    private String studentEmail;
    private String studentName;
    private String studentPhone;
    private boolean isUsed;
    private LocalDateTime validFrom;
    private LocalDateTime expiresAt;
    private String examTitle; // Optional, to show what the token is for
}
