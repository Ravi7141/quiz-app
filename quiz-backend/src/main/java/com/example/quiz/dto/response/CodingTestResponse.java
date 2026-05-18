package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response shape for coding test listing.
 *
 * Sample JSON:
 * {
 *   "id": 1,
 *   "title": "Two Sum",
 *   "description": "Find two numbers that add to target",
 *   "sampleInput": "nums=[2,7,11,15], target=9",
 *   "sampleOutput": "[0,1]",
 *   "difficulty": "EASY",
 *   "createdAt": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTestResponse {
    private Long id;
    private String title;
    private String description;
    private String sampleInput;
    private String sampleOutput;
    private String difficulty;
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
}
