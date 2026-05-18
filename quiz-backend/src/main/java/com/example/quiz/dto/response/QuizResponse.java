package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response shape for quiz data.
 *
 * Sample JSON:
 * {
 *   "id": 1,
 *   "title": "Java Fundamentals",
 *   "description": "Basic Java concepts",
 *   "durationMinutes": 30,
 *   "totalMarks": 100,
 *   "active": true,
 *   "createdAt": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Boolean active;
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
}
