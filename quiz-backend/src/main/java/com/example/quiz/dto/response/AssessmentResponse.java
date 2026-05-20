package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String shareToken;
    private Boolean active;
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private Integer passingPercentage;
    private LocalDateTime createdAt;
}
