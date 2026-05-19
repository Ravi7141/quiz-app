package com.example.quiz.dto.response;

import com.example.quiz.enums.AttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAttemptResponse {
    private Long attemptId;
    private Long assessmentId;
    private AttemptStatus status;
    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Map<Long, Long> quizAttemptMap;
}
