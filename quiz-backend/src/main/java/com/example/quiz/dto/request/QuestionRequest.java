package com.example.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for POST /admin/questions
 *
 * Sample JSON:
 * {
 *   "questionText": "What is JVM?",
 *   "optionA": "Java Virtual Machine",
 *   "optionB": "Java Variable Module",
 *   "optionC": "Java Version Manager",
 *   "optionD": "Just Virtual Machine",
 *   "correctAnswer": "A",
 *   "marks": 5,
 *   "quizId": 1
 * }
 */
@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    @NotBlank(message = "Correct answer is required")
    @Pattern(regexp = "^[ABCD]$", message = "Correct answer must be A, B, C, or D")
    private String correctAnswer;

    @NotNull(message = "Marks are required")
    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;
}
