package com.example.quiz.controller;

import com.example.quiz.dto.request.SubmitAnswerRequest;
import com.example.quiz.dto.request.CodingSubmissionRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.AttemptResponse;
import com.example.quiz.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 4 — Quiz Attempt Controller (No Security)
 *
 * ─────────────────────────────────────────────────────────────
 * POST /student/quizzes/start/{quizId}?studentId=1
 *   Response 201:
 *     { "success": true, "message": "Quiz started successfully",
 *       "data": { "attemptId": 1, "quizId": 2,
 *                 "quizTitle": "Java Fundamentals",
 *                 "status": "IN_PROGRESS",
 *                 "startedAt": "2024-01-01T10:00:00" } }
 *
 * POST /student/quizzes/submit-answer
 *   Request:
 *     { "attemptId": 1, "questionId": 3, "selectedOption": "B" }
 *   Response 200:
 *     { "success": true, "message": "Answer recorded successfully" }
 *
 * POST /student/quizzes/submit/{attemptId}
 *   Response 200:
 *     { "success": true, "message": "Quiz submitted successfully",
 *       "data": { "attemptId": 1, "status": "SUBMITTED",
 *                 "submittedAt": "2024-01-01T10:30:00" } }
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/student/quizzes")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    /**
     * POST /student/quizzes/start/{quizId}?studentId=1
     *
     * studentId is passed as a request parameter because there is no
     * authentication/session. The frontend should store the ID returned
     * from /login and pass it here.
     */
    @PostMapping("/start/{quizId}")
    public ResponseEntity<ApiResponse<AttemptResponse>> startQuiz(
            @PathVariable Long quizId,
            @RequestParam Long studentId
    ) {
        AttemptResponse data = attemptService.startAttempt(quizId, studentId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz started successfully", data));
    }

    /**
     * POST /student/quizzes/submit-answer
     */
    @PostMapping("/submit-answer")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request
    ) {
        attemptService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponse.success("Answer recorded successfully"));
    }

    /**
     * POST /student/quizzes/submit/{attemptId}
     */
    @PostMapping("/submit/{attemptId}")
    public ResponseEntity<ApiResponse<AttemptResponse>> submitQuiz(
            @PathVariable Long attemptId
    ) {
        AttemptResponse data = attemptService.submitAttempt(attemptId);
        return ResponseEntity.ok(ApiResponse.success("Quiz submitted successfully", data));
    }

    /**
     * POST /student/quizzes/submit-coding
     */
    @PostMapping("/submit-coding")
    public ResponseEntity<ApiResponse<Void>> submitCoding(
            @Valid @RequestBody CodingSubmissionRequest request
    ) {
        attemptService.saveCodingSubmission(request);
        return ResponseEntity.ok(ApiResponse.success("Coding submission recorded successfully"));
    }
}
