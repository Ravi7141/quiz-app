package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TokenGenerateRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotEmpty(message = "Exam type is required (QUIZ or CODING)")
    private String examType;

    @NotEmpty(message = "At least one student email is required")
    private List<String> emails;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
