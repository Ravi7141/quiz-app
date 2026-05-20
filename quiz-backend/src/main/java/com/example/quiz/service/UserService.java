package com.example.quiz.service;

import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UserService — centralised user management helpers.
 *
 * Replaces the duplicate "find or create student" logic that was
 * copy-pasted in ExamTokenService (x2) and AssessmentService.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find a student by email. If not found, create a new STUDENT account
     * with a random (hashed) password (they use token-based entry, not login).
     * Updates name/phone if the existing record is missing them.
     *
     * @param email      student email (required)
     * @param name       student full name (optional)
     * @param phone      student phone (optional)
     * @param createdBy  the admin who is importing (may be null)
     * @return the found or newly created User
     */
    public User findOrCreateStudent(String email, String name, String phone, User createdBy) {
        return userRepository.findByEmail(email).map(u -> {
            boolean changed = false;
            if ((u.getPhone() == null || u.getPhone().isEmpty()) && phone != null) {
                u.setPhone(phone);
                changed = true;
            }
            String derivedName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            if ((u.getName() == null || u.getName().isEmpty() || u.getName().equals(email.split("@")[0])) && name != null && !name.isEmpty()) {
                u.setName(name);
                changed = true;
            }
            if (u.getCreatedBy() == null && createdBy != null) {
                u.setCreatedBy(createdBy);
                changed = true;
            }
            if (changed) userRepository.save(u);
            return u;
        }).orElseGet(() -> {
            String derivedName = (name != null && !name.isEmpty()) ? name : email.split("@")[0];
            User newUser = User.builder()
                    .name(derivedName)
                    .email(email)
                    .phone(phone)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.STUDENT)
                    .createdBy(createdBy)
                    .build();
            return userRepository.save(newUser);
        });
    }

    /**
     * Delete a student by ID. Throws ResourceNotFoundException if not found.
     */
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        userRepository.delete(user);
    }

    /**
     * Map User → AuthResponse.
     */
    public AuthResponse mapToAuthResponse(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
