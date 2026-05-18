package com.example.quiz.controller;

import com.example.quiz.dto.request.QuestionRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.QuestionResponse;
import com.example.quiz.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Question Controller — student GET (answers hidden) + admin full CRUD.
 *
 * ─────────────────────────────────────────────────────────────
 * POST   /admin/questions                    → add question (answer visible)
 * GET    /admin/questions/quiz/{quizId}      → all questions for admin (answer visible)
 * PUT    /admin/questions/{id}               → update question
 * DELETE /admin/questions/{id}               → delete question
 *
 * GET    /questions/quiz/{quizId}            → questions for students (answer hidden)
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@Validated
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // ─── Admin Endpoints ─────────────────────────────────────────────────────

    /**
     * POST /admin/questions
     */
    @PostMapping("/admin/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(
            @Valid @RequestBody QuestionRequest request
    ) {
        QuestionResponse data = questionService.addQuestion(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Question added successfully", data));
    }

    @PostMapping("/admin/questions/bulk")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> addQuestionsBulk(
            @Valid @RequestBody List<QuestionRequest> request
    ) {
        List<QuestionResponse> data = questionService.addQuestionsBulk(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Questions imported successfully", data));
    }

    /**
     * GET /admin/questions/quiz/{quizId}
     * Returns questions WITH correct answers (for admin view).
     */
    @GetMapping("/admin/questions/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForAdmin(
            @PathVariable Long quizId
    ) {
        List<QuestionResponse> data = questionService.getQuestionsByQuizForAdmin(quizId);
        return ResponseEntity.ok(ApiResponse.success("Questions fetched for admin", data));
    }

    /**
     * PUT /admin/questions/{id}
     *
     * Sample request body (send only fields you want to change):
     * { "questionText": "Updated question?", "correctAnswer": "B" }
     */
    @PutMapping("/admin/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionRequest request
    ) {
        QuestionResponse data = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Question updated successfully", data));
    }

    /**
     * DELETE /admin/questions/{id}
     */
    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("Question deleted successfully"));
    }

    // ─── Student Endpoint ─────────────────────────────────────────────────────

    /**
     * GET /questions/quiz/{quizId}
     * Returns questions WITHOUT correct answer (student-safe).
     */
    @GetMapping("/questions/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getQuestionsForStudent(
            @PathVariable Long quizId
    ) {
        List<QuestionResponse> data = questionService.getQuestionsByQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success("Questions fetched successfully", data));
    }
}
