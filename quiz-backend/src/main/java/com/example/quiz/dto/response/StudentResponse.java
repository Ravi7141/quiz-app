package com.example.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response for GET /admin/students — student info without password.
 *
 * Sample JSON:
 * {
 *   "id": 2,
 *   "name": "Alice",
 *   "email": "alice@test.com",
 *   "role": "STUDENT",
 *   "createdAt": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
