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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Authentication Service — register, login, and getCurrentUser.
 *
 * Passwords are hashed with BCrypt. The Authorization header still
 * carries the user's email as a simple session token (no JWT yet).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieve the currently logged-in user from the Authorization header (email).
     */
    public User getCurrentUser() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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

    public AuthResponse registerUser(RegisterRequest request) {

        // Duplicate email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Role role = Role.STUDENT;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = Role.ADMIN;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .role(role)
                .build();

        User saved = userRepository.save(user);

        return AuthResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    public AuthResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with email: " + request.getEmail()));

        // BCrypt comparison with migration fallback
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            if (request.getPassword().equals(user.getPassword())) {
                // Auto-migrate plain-text password to BCrypt
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            } else {
                throw new BadRequestException("Invalid password");
            }
        }


        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
