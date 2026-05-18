package com.example.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request body for POST /admin/quizzes
 *
 * Sample JSON:
 * {
 *   "title": "Java Fundamentals",
 *   "description": "Basic Java concepts",
 *   "durationMinutes": 30,
 *   "totalMarks": 100,
 *   "active": true
 * }
 */
@Data
public class QuizRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Total marks are required")
    @Min(value = 1, message = "Total marks must be at least 1")
    private Integer totalMarks;

    private Boolean active;

    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
}
