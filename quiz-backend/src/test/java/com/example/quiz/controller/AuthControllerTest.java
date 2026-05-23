package com.example.quiz.controller;

import com.example.quiz.dto.request.LoginRequest;
import com.example.quiz.dto.request.RegisterRequest;
import com.example.quiz.dto.response.ApiResponse;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.example.quiz.service.OtpService otpService;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ========== Register Tests ==========

    @Test
    public void testRegisterSuccess() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registered successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    public void testRegisterMissingName() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setRole("STUDENT");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterMissingEmail() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setPassword("password123");
        invalidRequest.setRole("STUDENT");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterInvalidEmail() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setEmail("notanemail");
        invalidRequest.setPassword("password123");
        invalidRequest.setRole("STUDENT");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterPasswordTooShort() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setName("John Doe");
        invalidRequest.setEmail("john@example.com");
        invalidRequest.setPassword("short");
        invalidRequest.setRole("STUDENT");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterDuplicateEmail() throws Exception {
        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email already registered: john@example.com"));

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterAsAdmin() throws Exception {
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

        when(authService.registerUser(any(RegisterRequest.class)))
                .thenReturn(adminResponse);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    // ========== Login Tests ==========

    @Test
    public void testLoginSuccess() throws Exception {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testLoginMissingEmail() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginMissingPassword() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("john@example.com");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginInvalidEmail() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("notanemail");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginUnknownEmail() throws Exception {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new ResourceNotFoundException("No account found with email: unknown@example.com"));

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid password"));

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginEmptyBody() throws Exception {
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

