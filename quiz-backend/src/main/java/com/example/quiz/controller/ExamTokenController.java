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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ExamTokenController {

    private final ExamTokenService examTokenService;

    // ─── Admin Endpoints ─────────────────────────────────────────────────────

    @PostMapping("/admin/tokens/generate")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> generateTokens(@Valid @RequestBody TokenGenerateRequest request) {
        List<TokenResponse> data = examTokenService.generateTokens(request);
        log.info("TOKEN GENERATE: Type [{}] ID [{}] Count [{}]", request.getExamType(), request.getExamId(), request.getNumberOfTokens());
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

    @PostMapping("/admin/tokens/exam/{type}/{id}/email-all")
    public ResponseEntity<ApiResponse<Void>> emailAllTokens(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestParam String baseUrl
    ) {
        examTokenService.emailAllTokens(type, id, baseUrl);
        log.info("TOKEN EMAIL ALL: Type [{}] ID [{}]", type, id);
        return ResponseEntity.ok(ApiResponse.success("Emails sent successfully", null));
    }

    // ─── Public / Student Endpoints ──────────────────────────────────────────

    @GetMapping("/tokens/verify")
    public ResponseEntity<ApiResponse<TokenVerifyResponse>> verifyToken(@RequestParam String token) {
        try {
            TokenVerifyResponse data = examTokenService.verifyToken(token);
            return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/tokens/consume")
    public ResponseEntity<ApiResponse<Void>> consumeToken(@RequestParam String token) {
        try {
            examTokenService.consumeToken(token);
            log.info("TOKEN CONSUME: Token [{}]", token);
            return ResponseEntity.ok(ApiResponse.success("Token consumed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
