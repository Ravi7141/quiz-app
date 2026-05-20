package com.example.quiz.controller;

import com.example.quiz.dto.request.CodeRunRequest;
import com.example.quiz.dto.request.CodingTestRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.CodingTestResponse;
import com.example.quiz.service.CodingTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CodingTest Controller — student listing + code run/submit + admin CRUD.
 *
 * ─────────────────────────────────────────────────────────────
 * GET    /student/coding-tests              → list all problems
 * POST   /student/code/run                  → run code (simulated)
 * POST   /student/code/submit               → submit code (simulated)
 *
 * POST   /admin/coding-tests                → create problem
 * PUT    /admin/coding-tests/{id}           → update problem
 * DELETE /admin/coding-tests/{id}           → delete problem
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequiredArgsConstructor
public class CodingTestController {

    private final CodingTestService codingTestService;

    // ─── Student Endpoints ────────────────────────────────────────────────────

    /**
     * GET /student/coding-tests
     */
    @GetMapping("/student/coding-tests")
    public ResponseEntity<ApiResponse<List<CodingTestResponse>>> getCodingTests() {
        List<CodingTestResponse> data = codingTestService.getAllCodingTests();
        return ResponseEntity.ok(ApiResponse.success("Coding tests fetched successfully", data));
    }

    /**
     * GET /student/coding-tests/{id}
     */
    @GetMapping("/student/coding-tests/{id}")
    public ResponseEntity<ApiResponse<CodingTestResponse>> getCodingTestById(@PathVariable Long id) {
        CodingTestResponse data = codingTestService.getCodingTestById(id);
        return ResponseEntity.ok(ApiResponse.success("Coding test fetched successfully", data));
    }

    /**
     * GET /admin/coding-tests/{id} — admin detail view
     */
    @GetMapping("/admin/coding-tests/{id}")
    public ResponseEntity<ApiResponse<CodingTestResponse>> getCodingTestByIdAdmin(@PathVariable Long id) {
        CodingTestResponse data = codingTestService.getCodingTestById(id);
        return ResponseEntity.ok(ApiResponse.success("Coding test fetched successfully", data));
    }

    /**
     * POST /student/code/run
     */
    @PostMapping("/student/code/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runCode(
            @Valid @RequestBody CodeRunRequest request
    ) {
        Map<String, Object> data = codingTestService.runCode(request);
        return ResponseEntity.ok(ApiResponse.success("Code executed successfully", data));
    }

    /**
     * POST /student/code/submit
     */
    @PostMapping("/student/code/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitCode(
            @Valid @RequestBody CodeRunRequest request
    ) {
        Map<String, Object> data = codingTestService.submitCode(request);
        return ResponseEntity.ok(ApiResponse.success("Code submitted successfully", data));
    }

    // ─── Admin Endpoints ─────────────────────────────────────────────────────

    /**
     * POST /admin/coding-tests
     *
     * Sample request body:
     * { "title": "Two Sum", "description": "Find two numbers...",
     *   "sampleInput": "nums=[2,7], target=9",
     *   "sampleOutput": "[0,1]", "difficulty": "EASY" }
     */
    @PostMapping("/admin/coding-tests")
    public ResponseEntity<ApiResponse<CodingTestResponse>> createCodingTest(
            @Valid @RequestBody CodingTestRequest request
    ) {
        CodingTestResponse data = codingTestService.createCodingTest(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coding test created successfully", data));
    }

    /**
     * PUT /admin/coding-tests/{id}
     */
    @PutMapping("/admin/coding-tests/{id}")
    public ResponseEntity<ApiResponse<CodingTestResponse>> updateCodingTest(
            @PathVariable Long id,
            @RequestBody CodingTestRequest request
    ) {
        CodingTestResponse data = codingTestService.updateCodingTest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coding test updated successfully", data));
    }

    /**
     * DELETE /admin/coding-tests/{id}
     */
    @DeleteMapping("/admin/coding-tests/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCodingTest(@PathVariable Long id) {
        codingTestService.deleteCodingTest(id);
        return ResponseEntity.ok(ApiResponse.success("Coding test deleted successfully"));
    }

    /**
     * GET /admin/coding-tests/import-leetcode
     */
    @GetMapping("/admin/coding-tests/import-leetcode")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importLeetCodeQuestion(
            @RequestParam String query
    ) {
        Map<String, Object> data = codingTestService.importLeetCodeQuestion(query);
        return ResponseEntity.ok(ApiResponse.success("LeetCode question imported successfully", data));
    }
}
