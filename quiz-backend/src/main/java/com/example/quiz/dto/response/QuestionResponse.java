package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response shape for a single MCQ question.
 * Note: correctAnswer is intentionally NOT included when serving questions
 * to students (the service controls this).
 *
 * Sample JSON (student-facing):
 * {
 *   "id": 1,
 *   "questionText": "What is JVM?",
 *   "optionA": "Java Virtual Machine",
 *   "optionB": "Java Variable Module",
 *   "optionC": "Java Version Manager",
 *   "optionD": "Just Virtual Machine",
 *   "marks": 5,
 *   "quizId": 1
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; // set to null for student responses
    private Integer marks;
    private Long quizId;
    /** True if the question has multiple correct answers (comma-separated correctAnswer) */
    private Boolean multiAnswer;
}
