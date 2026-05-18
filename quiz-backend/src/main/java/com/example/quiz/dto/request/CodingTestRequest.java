package com.example.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request body for POST /admin/coding-tests and PUT /admin/coding-tests/{id}
 *
 * Sample JSON:
 * {
 *   "title": "Two Sum",
 *   "description": "Find two numbers that add up to the target.",
 *   "sampleInput": "nums=[2,7,11,15], target=9",
 *   "sampleOutput": "[0,1]",
 *   "difficulty": "EASY"
 * }
 */
@Data
public class CodingTestRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String sampleInput;

    private String sampleOutput;

    @NotBlank(message = "Difficulty is required")
    private String difficulty; // EASY, MEDIUM, HARD

    private java.time.LocalDateTime scheduledFor;
    private java.time.LocalDateTime validUntil;
}
