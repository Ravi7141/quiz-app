package com.example.quiz.dto.response;

import com.example.quiz.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response returned when a student starts or submits a quiz attempt.
 *
 * Sample JSON:
 * {
 *   "attemptId": 1,
 *   "quizId": 2,
 *   "quizTitle": "Java Fundamentals",
 *   "status": "IN_PROGRESS",
 *   "startedAt": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private AttemptStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
