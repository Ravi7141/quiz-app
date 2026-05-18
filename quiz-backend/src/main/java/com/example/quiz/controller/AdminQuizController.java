package com.example.quiz.controller;

import com.example.quiz.dto.request.QuizRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Quiz Controller — full CRUD.
 *
 * ─────────────────────────────────────────────────────────────
 * POST   /admin/quizzes          → create quiz
 * GET    /admin/quizzes          → get all quizzes (incl. inactive)
 * PUT    /admin/quizzes/{id}     → update quiz
 * DELETE /admin/quizzes/{id}     → delete quiz
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizService quizService;

    /**
     * POST /admin/quizzes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody QuizRequest request
    ) {
        QuizResponse data = quizService.createQuiz(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz created successfully", data));
    }

    /**
     * GET /admin/quizzes — returns ALL quizzes (active + inactive)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getAllQuizzes() {
        List<QuizResponse> data = quizService.getAllQuizzes();
        return ResponseEntity.ok(ApiResponse.success("All quizzes fetched successfully", data));
    }

    /**
     * GET /admin/quizzes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(@PathVariable Long id) {
        QuizResponse data = quizService.getQuizById(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz fetched successfully", data));
    }

    /**
     * PUT /admin/quizzes/{id}
     *
     * Sample request body (send only fields you want to change):
     * { "title": "Updated Title", "active": false }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long id,
            @RequestBody QuizRequest request
    ) {
        QuizResponse data = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(ApiResponse.success("Quiz updated successfully", data));
    }

    /**
     * DELETE /admin/quizzes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted successfully"));
    }
}
