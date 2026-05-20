package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /student/quizzes/submit-answer
 *
 * selectedOption can be:
 *  - single: "A"
 *  - multi:  "A,C"  (comma-separated, for multi-answer questions)
 *  - empty:  ""     (student deselected)
 */
@Data
public class SubmitAnswerRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    // No @NotBlank / @Pattern — allows multi-answer "A,C" and empty deselect ""
    private String selectedOption;
}
