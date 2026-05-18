package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /student/code/run  and  POST /student/code/submit
 *
 * Sample JSON:
 * {
 *   "codingTestId": 1,
 *   "language": "JAVA",
 *   "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello\"); } }"
 * }
 */
@Data
public class CodeRunRequest {

    @NotNull(message = "Coding test ID is required")
    private Long codingTestId;

    @NotBlank(message = "Language is required")
    private String language;   // e.g. JAVA, PYTHON, CPP

    @NotBlank(message = "Code is required")
    private String code;
}
