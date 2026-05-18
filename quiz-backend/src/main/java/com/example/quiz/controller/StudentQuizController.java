package com.example.quiz.controller;

import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 2 — Student Quiz Controller (No Security)
 *
 * ─────────────────────────────────────────────────────────────
 * GET /student/quizzes
 *   Response 200:
 *     { "success": true, "message": "Quizzes fetched successfully",
 *       "data": [ { "id": 1, "title": "Java Fundamentals", ... }, ... ] }
 *
 * GET /student/quizzes/{id}
 *   Response 200:
 *     { "success": true, "message": "Quiz fetched successfully",
 *       "data": { "id": 1, "title": "Java Fundamentals", ... } }
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/student/quizzes")
@RequiredArgsConstructor
public class StudentQuizController {

    private final QuizService quizService;

    /**
     * GET /student/quizzes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzes() {
        List<QuizResponse> data = quizService.getActiveQuizzes();
        return ResponseEntity.ok(ApiResponse.success("Quizzes fetched successfully", data));
    }

    /**
     * GET /student/quizzes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(@PathVariable Long id) {
        QuizResponse data = quizService.getQuizById(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz fetched successfully", data));
    }
}
