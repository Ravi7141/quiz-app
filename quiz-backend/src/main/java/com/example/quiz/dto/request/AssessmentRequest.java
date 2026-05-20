package com.example.quiz.dto.request;

import com.example.quiz.enums.SectionType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentRequest {

    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private Integer passingPercentage;
    private List<SectionRequest> sections;

    @Data
    public static class SectionRequest {
        private SectionType type;
        private Long referenceId;
        private Integer order;
    }
}
