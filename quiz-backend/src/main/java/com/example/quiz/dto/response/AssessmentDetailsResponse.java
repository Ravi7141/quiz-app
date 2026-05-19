package com.example.quiz.dto.response;

import com.example.quiz.enums.SectionType;
import lombok.Builder;
import lombok.Data;
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
