package com.example.quiz.controller;

import com.example.quiz.dto.request.AssessmentRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.AssessmentResponse;
import com.example.quiz.dto.response.AssessmentDetailsResponse;
import com.example.quiz.dto.response.AssessmentAttemptResponse;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssessmentController {

    private final AssessmentService assessmentService;

    // ─── ADMIN APIs ──────────────────────────────────────────────────────────

    @PostMapping("/admin/assessments")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(@RequestBody AssessmentRequest request) {
        AssessmentResponse response = assessmentService.createAssessment(request);
        return ResponseEntity.ok(ApiResponse.success("Assessment created successfully", response));
    }

    @GetMapping("/admin/assessments")
    public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getAllAssessments() {
        List<AssessmentResponse> response = assessmentService.getAllAssessments();
        return ResponseEntity.ok(ApiResponse.success("Assessments retrieved successfully", response));
    }

    @PostMapping("/admin/assessments/{id}/share")
    public ResponseEntity<ApiResponse<AssessmentResponse>> regenerateShareToken(@PathVariable Long id) {
        AssessmentResponse response = assessmentService.regenerateShareToken(id);
        return ResponseEntity.ok(ApiResponse.success("Share token regenerated successfully", response));
    }

    // ─── STUDENT APIs ────────────────────────────────────────────────────────

    @GetMapping("/assessment/{token}")
    public ResponseEntity<ApiResponse<AssessmentDetailsResponse>> getAssessmentByToken(@PathVariable String token) {
        AssessmentDetailsResponse response = assessmentService.getAssessmentByToken(token);
        return ResponseEntity.ok(ApiResponse.success("Assessment details retrieved successfully", response));
    }

    @PostMapping("/assessment/start")
    public ResponseEntity<ApiResponse<AssessmentAttemptResponse>> startAttempt(
            @RequestParam Long assessmentId,
            @RequestParam Long studentId) {
        AssessmentAttemptResponse response = assessmentService.startAttempt(assessmentId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment attempt started successfully", response));
    }

    @PostMapping("/assessment/submit")
    public ResponseEntity<ApiResponse<AssessmentAttemptResponse>> submitAttempt(
            @RequestParam Long attemptId) {
        AssessmentAttemptResponse response = assessmentService.submitAttempt(attemptId);
        return ResponseEntity.ok(ApiResponse.success("Assessment attempt submitted successfully", response));
    }

    @PostMapping("/assessment/submit-coding")
    public ResponseEntity<ApiResponse<Void>> saveCodingSubmission(
            @RequestParam Long assessmentAttemptId,
            @RequestParam Long codingTestId,
            @RequestParam String code,
            @RequestParam String language,
            @RequestParam Boolean passed) {
        assessmentService.saveCodingSubmission(assessmentAttemptId, codingTestId, code, language, passed);
        return ResponseEntity.ok(ApiResponse.success("Coding submission saved successfully", null));
    }

    @PostMapping("/assessment/enroll")
    public ResponseEntity<ApiResponse<AuthResponse>> enrollStudent(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String phone) {
        AuthResponse response = assessmentService.enrollStudent(name, email, phone);
        return ResponseEntity.ok(ApiResponse.success("Student enrolled successfully", response));
    }
}
