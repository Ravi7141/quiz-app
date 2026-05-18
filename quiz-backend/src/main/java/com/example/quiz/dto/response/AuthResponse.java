package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for POST /register and POST /login
 *
 * Sample JSON:
 * {
 *   "id": 1,
 *   "name": "Alice",
 *   "email": "alice@example.com",
 *   "role": "STUDENT"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
}
