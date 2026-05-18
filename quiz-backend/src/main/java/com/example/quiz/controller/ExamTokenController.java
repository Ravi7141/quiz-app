package com.example.quiz.controller;

import com.example.quiz.dto.request.TokenGenerateRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.TokenResponse;
import com.example.quiz.dto.response.TokenVerifyResponse;
import com.example.quiz.service.ExamTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExamTokenController {

    private final ExamTokenService examTokenService;

    // ─── Admin Endpoints ─────────────────────────────────────────────────────

    @PostMapping("/admin/tokens/generate")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> generateTokens(@Valid @RequestBody TokenGenerateRequest request) {
        List<TokenResponse> data = examTokenService.generateTokens(request);
        return ResponseEntity.ok(ApiResponse.success("Tokens generated successfully", data));
    }

    @GetMapping("/admin/tokens/exam/{type}/{id}")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> getTokensForExam(
            @PathVariable String type,
            @PathVariable Long id
    ) {
        List<TokenResponse> data = examTokenService.getTokensForExam(type, id);
        return ResponseEntity.ok(ApiResponse.success("Tokens fetched successfully", data));
    }

    // ─── Public / Student Endpoints ──────────────────────────────────────────

    @GetMapping("/api/tokens/verify")
    public ResponseEntity<ApiResponse<TokenVerifyResponse>> verifyToken(@RequestParam String token) {
        try {
            TokenVerifyResponse data = examTokenService.verifyToken(token);
            return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/api/tokens/consume")
    public ResponseEntity<ApiResponse<Void>> consumeToken(@RequestParam String token) {
        try {
            examTokenService.consumeToken(token);
            return ResponseEntity.ok(ApiResponse.success("Token consumed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
