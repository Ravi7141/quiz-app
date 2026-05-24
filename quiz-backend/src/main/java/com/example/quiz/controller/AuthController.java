package com.example.quiz.controller;

import com.example.quiz.dto.request.LoginRequest;
import com.example.quiz.dto.request.RegisterRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.example.quiz.service.OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            AuthResponse data = authService.registerUser(request);
            log.info("USER REGISTER: Success for email [{}]", request.getEmail());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registered successfully", data));
        } catch (Exception e) {
            log.error("USER REGISTER FAIL: email [{}] - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            AuthResponse data = authService.loginUser(request);
            log.info("USER LOGIN: Success for email [{}]", request.getEmail());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("Login successful", data));
        } catch (Exception e) {
            log.error("USER LOGIN FAIL: email [{}] - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ApiResponse<Void>> requestForgotPassword(
            @Valid @RequestBody com.example.quiz.dto.request.ForgotPasswordRequest request
    ) {
        log.info("PASSWORD RESET REQUEST: email [{}]", request.getEmail());
        otpService.generateAndSendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification code sent to your email address", null));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.example.quiz.dto.request.ResetPasswordRequest request
    ) {
        log.info("PASSWORD RESET COMPLETE: email [{}]", request.getEmail());
        otpService.verifyOtpAndResetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
