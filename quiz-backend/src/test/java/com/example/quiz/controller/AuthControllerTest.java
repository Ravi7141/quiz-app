package com.example.quiz.controller;

import com.example.quiz.dto.request.LoginRequest;
import com.example.quiz.dto.request.RegisterRequest;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;
    
    @Mock
    private com.example.quiz.service.OtpService otpService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setRole("STUDENT");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("password123");

        authResponse = AuthResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role("STUDENT")
                .build();
    }

    @Test
    public void testRegisterSuccess() {
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);
        var result = authController.register(validRegisterRequest);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    public void testRegisterDuplicateEmail() {
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email already registered"));
        assertThrows(BadRequestException.class, () -> authController.register(validRegisterRequest));
    }

    @Test
    public void testLoginSuccess() {
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);
        var result = authController.login(validLoginRequest);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    public void testLoginWrongPassword() {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid password"));
        assertThrows(BadRequestException.class, () -> authController.login(validLoginRequest));
    }

    @Test
    public void testLoginUnknownEmail() {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new ResourceNotFoundException("No account found"));
        assertThrows(ResourceNotFoundException.class, () -> authController.login(validLoginRequest));
    }

    @Test
    public void testRegisterAsAdmin() {
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setName("Admin User");
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPassword("adminpass123");
        adminRequest.setRole("ADMIN");

        AuthResponse adminResponse = AuthResponse.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .role("ADMIN")
                .build();

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(adminResponse);
        var result = authController.register(adminRequest);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("ADMIN", result.getBody().getData().getRole());
    }
}
