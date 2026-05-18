package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for POST /student/quizzes/submit-answer
 *
 * Sample JSON:
 * {
 *   "attemptId": 1,
 *   "questionId": 3,
 *   "selectedOption": "B"
 * }
 */
@Data
public class SubmitAnswerRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Selected option is required")
    @Pattern(regexp = "^[ABCD]$", message = "Selected option must be A, B, C, or D")
    private String selectedOption;
}
