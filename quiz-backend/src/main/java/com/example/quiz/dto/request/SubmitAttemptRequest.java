package com.example.quiz.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class SubmitAttemptRequest {
    private Long attemptId;
    /**
     * Map of questionId -> selectedOption (e.g. "A" or "A,C").
     * Sent by the frontend at final submission so the backend can
     * persist answers directly without relying on per-click saves.
     * NOTE: keys are Strings from JSON; parsed to Long in service.
     */
    private Map<String, String> answers;
}
