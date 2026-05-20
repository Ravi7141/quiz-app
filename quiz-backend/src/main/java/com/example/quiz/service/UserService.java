package com.example.quiz.service;

import com.example.quiz.dto.response.AuthResponse;
import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.*;
import com.example.quiz.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentCodingSubmissionRepository studentCodingSubmissionRepository;
    private final ExamTokenRepository examTokenRepository;

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
    @Transactional
    public void deleteStudent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
                
        // 1. ExamTokens
        List<ExamToken> tokens = examTokenRepository.findByStudentEmail(user.getEmail());
        examTokenRepository.deleteAll(tokens);
        
        // 2. StudentAnswers (via QuizAttempts)
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByStudentId(id);
        for (QuizAttempt qa : quizAttempts) {
            studentAnswerRepository.deleteByAttemptId(qa.getId());
        }
        quizAttemptRepository.deleteAll(quizAttempts);

        // 3. StudentCodingSubmissions (via AssessmentAttempts)
        List<AssessmentAttempt> assessmentAttempts = assessmentAttemptRepository.findByStudentId(id);
        for (AssessmentAttempt aa : assessmentAttempts) {
            studentCodingSubmissionRepository.deleteByAssessmentAttemptId(aa.getId());
        }
        assessmentAttemptRepository.deleteAll(assessmentAttempts);

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
