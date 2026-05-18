package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full result breakdown returned after quiz submission.
 *
 * Sample JSON:
 * {
 *   "attemptId": 1,
 *   "quizTitle": "Java Fundamentals",
 *   "studentName": "Alice",
 *   "totalQuestions": 10,
 *   "correctAnswers": 7,
 *   "score": 70,
 *   "totalMarks": 100,
 *   "percentage": 70.0,
 *   "passed": true
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {
    private Long attemptId;
    private String quizTitle;
    private String studentName;
    private int totalQuestions;
    private int correctAnswers;
    private int score;
    private int totalMarks;
    private double percentage;
    private boolean passed;
}
