package com.example.quiz.service;

import com.example.quiz.dto.request.LoginRequest;
import com.example.quiz.dto.request.RegisterRequest;
import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Phase 1 — Authentication Service (No Security / No JWT)
 *
 * Handles:
 *  - POST /register → registerUser()
 *  - POST /login    → loginUser()
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Retrieve the currently logged-in user from the Request Context's Authorization header (email).
     */
    public User getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String email = authHeader.substring(7).trim();
                return userRepository.findByEmail(email).orElse(null);
            }
        }
        return null;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     *
     * Steps:
     *  1. Check if email is already taken → throw BadRequestException.
     *  2. Determine role (defaults to STUDENT if not provided).
     *  3. Save the User entity with plain-text password.
     *  4. Return AuthResponse with user details.
     */
    public AuthResponse registerUser(RegisterRequest request) {

        // Step 1 — Duplicate email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Step 2 — Determine role
        Role role = Role.STUDENT;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }

        // Step 3 — Build and save user (plain-text password)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(role)
                .build();

        User saved = userRepository.save(user);

        // Step 4 — Return response
        return AuthResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    /**
     * Authenticate an existing user.
     *
     * Steps:
     *  1. Find user by email → throw ResourceNotFoundException if not found.
     *  2. Compare plain-text passwords directly.
     *  3. Return AuthResponse with user details.
     */
    public AuthResponse loginUser(LoginRequest request) {

        // Step 1 — Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with email: " + request.getEmail()));

        // Step 2 — Verify password (plain-text comparison)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        // Step 3 — Return response
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
