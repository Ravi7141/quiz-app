package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for GET /admin/stats
 *
 * Sample JSON:
 * {
 *   "totalQuizzes": 5,
 *   "totalQuestions": 40,
 *   "totalStudents": 12,
 *   "totalAttempts": 30
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalQuizzes;
    private long totalAssessments;
    private long totalQuestions;
    private long totalStudents;
    private long totalAttempts;
}
