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

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.example.quiz.service.OtpService otpService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse data = authService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", data));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse data = authService.loginUser(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", data));
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ApiResponse<Void>> requestForgotPassword(
            @Valid @RequestBody com.example.quiz.dto.request.ForgotPasswordRequest request
    ) {
        otpService.generateAndSendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification code sent to your email address", null));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.example.quiz.dto.request.ResetPasswordRequest request
    ) {
        otpService.verifyOtpAndResetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
