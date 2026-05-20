package com.example.quiz.dto.response;

import com.example.quiz.enums.SectionType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AssessmentDetailsResponse {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String shareToken;
    private Boolean active;
    private LocalDateTime scheduledFor;
    private LocalDateTime validUntil;
    private Integer passingPercentage;
    private List<SectionDetails> sections;

    @Data
    @Builder
    public static class SectionDetails {
        private Long id;
        private SectionType type;
        private Long referenceId;
        private Integer order;
        private String title;
        private String description;
        private List<QuestionResponse> questions;
        private CodingTestResponse codingTest;
    }
}
