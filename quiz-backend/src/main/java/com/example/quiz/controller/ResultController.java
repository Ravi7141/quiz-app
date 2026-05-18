package com.example.quiz.controller;

import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.ResultResponse;
import com.example.quiz.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 5 — Result Controller (No Security)
 *
 * ─────────────────────────────────────────────────────────────
 * GET /student/results/{attemptId}
 *   Response 200:
 *     { "success": true, "message": "Result fetched successfully",
 *       "data": {
 *         "attemptId": 1,
 *         "quizTitle": "Java Fundamentals",
 *         "studentName": "Alice",
 *         "totalQuestions": 10,
 *         "correctAnswers": 7,
 *         "score": 70,
 *         "totalMarks": 100,
 *         "percentage": 70.0,
 *         "passed": true
 *       }
 *     }
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/student/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    /**
     * GET /student/results/{attemptId}
     */
    @GetMapping("/{attemptId}")
    public ResponseEntity<ApiResponse<ResultResponse>> getResult(
            @PathVariable Long attemptId
    ) {
        ResultResponse data = resultService.getResult(attemptId);
        return ResponseEntity.ok(ApiResponse.success("Result fetched successfully", data));
    }
}
