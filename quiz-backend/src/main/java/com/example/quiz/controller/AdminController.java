package com.example.quiz.controller;

import com.example.quiz.dto.response.AdminResultResponse;
import com.example.quiz.dto.response.AdminStatsResponse;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.StudentResponse;
import com.example.quiz.service.AdminService;
import com.example.quiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin General Controller — stats, students, all results.
 *
 * ─────────────────────────────────────────────────────────────
 * GET /admin/stats
 *   Response 200:
 *     { "success": true, "message": "Stats fetched successfully",
 *       "data": { "totalQuizzes": 5, "totalQuestions": 40,
 *                 "totalStudents": 12, "totalAttempts": 30 } }
 *
 * GET /admin/students
 *   Response 200:
 *     { "success": true, "message": "Students fetched successfully",
 *       "data": [ { "id": 2, "name": "Alice", "email": "...", "role": "STUDENT" } ] }
 *
 * GET /admin/results
 *   Response 200:
 *     { "success": true, "message": "Results fetched successfully",
 *       "data": [ { "attemptId": 1, "studentName": "Alice", ... } ] }
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    /**
     * GET /admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        AdminStatsResponse data = adminService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Stats fetched successfully", data));
    }

    /**
     * GET /admin/students  
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> data = adminService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Students fetched successfully", data));
    }

    /**
     * GET /admin/results
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<AdminResultResponse>>> getAllResults() {
        List<AdminResultResponse> data = adminService.getAllResults();
        return ResponseEntity.ok(ApiResponse.success("Results fetched successfully", data));
    }

    /**
     * GET /admin/students/{id}/results  — detailed view for one student
     */
    @GetMapping("/students/{id}/results")
    public ResponseEntity<ApiResponse<List<AdminResultResponse>>> getStudentResults(
            @PathVariable Long id
    ) {
        List<AdminResultResponse> data = adminService.getStudentResults(id);
        return ResponseEntity.ok(ApiResponse.success("Student results fetched", data));
    }

    /**
     * GET /admin/quizzes/{id}/results  — all attempts for a specific quiz
     */
    @GetMapping("/quizzes/{id}/results")
    public ResponseEntity<ApiResponse<List<AdminResultResponse>>> getQuizResults(
            @PathVariable Long id
    ) {
        List<AdminResultResponse> data = adminService.getQuizResults(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz results fetched", data));
    }

    /**
     * DELETE /admin/students/{id} — remove a student account
     */
    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deleted successfully"));
    }

    /**
     * GET /admin/assessments/{id}/results — all student submissions for an assessment
     */
    @GetMapping("/assessments/{id}/results")
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAssessmentResults(
            @PathVariable Long id
    ) {
        List<java.util.Map<String, Object>> data = adminService.getAssessmentResults(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment results fetched", data));
    }
}
