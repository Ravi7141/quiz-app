package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CodingSubmissionRequest {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    @NotNull(message = "Coding Test ID is required")
    private Long codingTestId;

    private String code;
    private String language;
    private Boolean passed;
}
