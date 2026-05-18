package com.example.quiz.dto.response;

import com.example.quiz.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response for GET /admin/results — all attempt records.
 *
 * Sample JSON:
 * {
 *   "attemptId": 1,
 *   "studentName": "Alice",
 *   "studentEmail": "alice@test.com",
 *   "quizTitle": "Java Fundamentals",
 *   "score": 15,
 *   "totalMarks": 20,
 *   "status": "SUBMITTED",
 *   "startedAt": "2024-01-01T10:00:00",
 *   "submittedAt": "2024-01-01T10:25:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResultResponse {
    private Long attemptId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer totalMarks;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private AttemptStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
