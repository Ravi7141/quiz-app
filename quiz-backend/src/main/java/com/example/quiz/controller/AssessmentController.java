package com.example.quiz.controller;

import com.example.quiz.dto.request.AssessmentRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.AssessmentResponse;
import com.example.quiz.dto.response.AssessmentDetailsResponse;
import com.example.quiz.dto.response.AssessmentAttemptResponse;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.service.AssessmentService;
import com.example.quiz.dto.request.SubmitAttemptRequest;
import lombok.Data;
import com.example.quiz.service.AssessmentService;
import com.example.quiz.dto.request.SubmitAttemptRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    // ─── ADMIN APIs ──────────────────────────────────────────────────────────

    @PostMapping("/admin/assessments")
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(@RequestBody AssessmentRequest request) {
        AssessmentResponse response = assessmentService.createAssessment(request);
        log.info("ASSESSMENT CREATE: Assessment ID [{}] Title [{}]", response.getId(), response.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Assessment created successfully", response));
    }

    @GetMapping("/admin/assessments")
    public ResponseEntity<ApiResponse<List<AssessmentResponse>>> getAllAssessments() {
        List<AssessmentResponse> response = assessmentService.getAllAssessments();
        return ResponseEntity.ok(ApiResponse.success("Assessments retrieved successfully", response));
    }

    @GetMapping("/admin/assessments/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> getAssessmentById(@PathVariable Long id) {
        AssessmentResponse response = assessmentService.getAssessmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Assessment retrieved successfully", response));
    }

    @PutMapping("/admin/assessments/{id}")
    public ResponseEntity<ApiResponse<AssessmentResponse>> updateAssessment(
            @PathVariable Long id,
            @RequestBody AssessmentRequest request) {
        AssessmentResponse response = assessmentService.updateAssessment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Assessment updated successfully", response));
    }

    @DeleteMapping("/admin/assessments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        log.info("ASSESSMENT DELETE: Assessment ID [{}]", id);
        return ResponseEntity.ok(ApiResponse.success("Assessment deleted successfully", null));
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
    public ResponseEntity<ApiResponse<AssessmentAttemptResponse>> submitAttempt(@RequestBody SubmitAttemptRequest body) {
        AssessmentAttemptResponse response = assessmentService.submitAttempt(body.getAttemptId(), body.getAnswers());
        log.info("ASSESSMENT SUBMIT: Attempt ID [{}] Score [{}]", response.getId(), response.getScore());
        return ResponseEntity.ok(ApiResponse.success("Assessment attempt submitted successfully", response));
    }

    @GetMapping("/assessment/last-error")
    public ResponseEntity<String> getLastError() {
        return ResponseEntity.ok(com.example.quiz.exception.GlobalExceptionHandler.lastExceptionTrace);
    }



    /** DEBUG endpoint — call GET /assessment/debug/{attemptId} to see quiz attempts and answers */
    @GetMapping("/assessment/debug/{attemptId}")
    public ResponseEntity<?> debugAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(assessmentService.debugAttempt(attemptId));
    }

    /** DEBUG endpoint — GET /assessment/debug/student/{studentId}/latest — finds latest attempt */
    @GetMapping("/assessment/debug/student/{studentId}/latest")
    public ResponseEntity<?> debugLatestAttempt(@PathVariable Long studentId) {
        return ResponseEntity.ok(assessmentService.debugLatestAttemptForStudent(studentId));
    }

    /**
     * Save coding submission — uses JSON body to avoid URL length limits for code.
     * Body: { assessmentAttemptId, codingTestId, code, language, passed }
     */
    @PostMapping("/assessment/submit-coding")
    public ResponseEntity<ApiResponse<Void>> saveCodingSubmission(
            @RequestBody CodingSubmissionBody body) {
        assessmentService.saveCodingSubmission(
                body.getAssessmentAttemptId(),
                body.getCodingTestId(),
                body.getCode(),
                body.getLanguage(),
                body.getPassed());
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

    /** Inner DTO for coding submission body */
    @Data
    public static class CodingSubmissionBody {
        private Long assessmentAttemptId;
        private Long codingTestId;
        private String code;
        private String language;
        private Boolean passed;
    }
}
