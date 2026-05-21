package com.example.quiz.service;

import com.example.quiz.dto.response.AdminResultResponse;
import com.example.quiz.dto.response.AdminStatsResponse;
import com.example.quiz.dto.response.StudentResponse;
import com.example.quiz.entity.AssessmentAttempt;
import com.example.quiz.entity.QuizAttempt;
import com.example.quiz.entity.User;
import com.example.quiz.enums.AttemptStatus;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminService — provides stats, student listing, and full result listing
 * for the admin panel.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final StudentAnswerRepository answerRepository;
    private final AuthService authService;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final AssessmentSectionRepository assessmentSectionRepository;
    private final AssessmentRepository assessmentRepository;

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    /**
     * GET /admin/stats
     * Returns total counts for admin dashboard overview cards.
     */
    public AdminStatsResponse getStats() {
        User currentAdmin = authService.getCurrentUser();
        if (currentAdmin != null) {
            return AdminStatsResponse.builder()
                    .totalQuizzes(quizRepository.countByCreatedById(currentAdmin.getId()))
                    .totalAssessments(assessmentRepository.countByCreatedById(currentAdmin.getId()))
                    .totalQuestions(questionRepository.countByQuizCreatedById(currentAdmin.getId()))
                    .totalStudents(userRepository.countByRoleAndCreatedById(Role.STUDENT, currentAdmin.getId()))
                    .totalAttempts(attemptRepository.countByQuizCreatedById(currentAdmin.getId()))
                    .build();
        }
        return AdminStatsResponse.builder()
                .totalQuizzes(quizRepository.count())
                .totalAssessments(assessmentRepository.count())
                .totalQuestions(questionRepository.count())
                .totalStudents(userRepository.countByRole(Role.STUDENT))
                .totalAttempts(attemptRepository.count())
                .build();
    }

    // ─── All Students ─────────────────────────────────────────────────────────

    /**
     * GET /admin/students
     * Returns all users with STUDENT role (no passwords).
     */
    public List<StudentResponse> getAllStudents() {
        User currentAdmin = authService.getCurrentUser();
        List<User> students;
        if (currentAdmin != null) {
            students = userRepository.findByRoleAndCreatedById(Role.STUDENT, currentAdmin.getId());
        } else {
            students = userRepository.findByRole(Role.STUDENT);
        }
        return students.stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    // ─── All Results / Attempts ───────────────────────────────────────────────

    /**
     * GET /admin/results
     * Returns all quiz attempts across all students and quizzes.
     */
    public List<AdminResultResponse> getAllResults() {
        User currentAdmin = authService.getCurrentUser();
        List<QuizAttempt> attempts;
        if (currentAdmin != null) {
            attempts = attemptRepository.findByQuizCreatedById(currentAdmin.getId());
        } else {
            attempts = attemptRepository.findAll();
        }
        return attempts.stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /admin/students/{id}/results
     * Returns all attempts for a specific student.
     */
    public List<AdminResultResponse> getStudentResults(Long studentId) {
        // validate student exists
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        User currentAdmin = authService.getCurrentUser();
        List<QuizAttempt> attempts = attemptRepository.findByStudentId(studentId);
        if (currentAdmin != null) {
            attempts = attempts.stream()
                    .filter(a -> a.getQuiz().getCreatedBy() != null && a.getQuiz().getCreatedBy().getId().equals(currentAdmin.getId()))
                    .collect(Collectors.toList());
        }
        return attempts.stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /admin/quizzes/{id}/results
     * Returns all attempts for a specific quiz.
     */
    public List<AdminResultResponse> getQuizResults(Long quizId) {
        User currentAdmin = authService.getCurrentUser();
        List<QuizAttempt> attempts = attemptRepository.findByQuizId(quizId);
        if (currentAdmin != null) {
            attempts = attempts.stream()
                    .filter(a -> a.getQuiz().getCreatedBy() != null && a.getQuiz().getCreatedBy().getId().equals(currentAdmin.getId()))
                    .collect(Collectors.toList());
        }
        return attempts.stream()
                .map(this::mapToResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET /admin/assessments/{id}/results
     * Returns all SUBMITTED assessment attempts for a given assessment.
     */
    public List<java.util.Map<String, Object>> getAssessmentResults(Long assessmentId) {
        List<AssessmentAttempt> attempts = assessmentAttemptRepository.findByAssessmentId(assessmentId)
                .stream()
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED)
                .collect(Collectors.toList());

        // Calculate total marks for this assessment
        List<com.example.quiz.entity.AssessmentSection> sections = assessmentSectionRepository
                .findByAssessmentIdOrderBySectionOrderAsc(assessmentId);
        int totalMarks = 0;
        for (com.example.quiz.entity.AssessmentSection section : sections) {
            if (section.getSectionType() == com.example.quiz.enums.SectionType.QUIZ && section.getReferenceId() != null) {
                com.example.quiz.entity.Quiz quiz = quizRepository.findById(section.getReferenceId()).orElse(null);
                if (quiz != null) {
                    int qMax = (quiz.getTotalMarks() != null && quiz.getTotalMarks() > 0)
                            ? quiz.getTotalMarks()
                            : questionRepository.findByQuizId(quiz.getId()).stream()
                              .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 1).sum();
                    totalMarks += qMax;
                }
            } else if (section.getSectionType() == com.example.quiz.enums.SectionType.CODING) {
                totalMarks += 20;
            }
        }
        final int finalTotalMarks = totalMarks;

        return attempts.stream().map(a -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("studentName", a.getStudent().getName());
            m.put("studentEmail", a.getStudent().getEmail());
            m.put("score", a.getScore() != null ? a.getScore() : 0);
            m.put("totalMarks", finalTotalMarks);
            m.put("percentage", a.getPercentage() != null ? a.getPercentage() : 0.0);
            m.put("passed", Boolean.TRUE.equals(a.getPassed()));
            m.put("submittedAt", a.getSubmittedAt());
            m.put("status", a.getStatus());
            return m;
        }).collect(Collectors.toList());
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private StudentResponse mapToStudentResponse(User user) {
        return StudentResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AdminResultResponse mapToResultResponse(QuizAttempt attempt) {
        List<com.example.quiz.entity.StudentAnswer> answers = answerRepository.findByAttemptId(attempt.getId());
        int correctCount = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        int totalQ = questionRepository.countByQuizId(attempt.getQuiz().getId());
        return AdminResultResponse.builder()
                .attemptId(attempt.getId())
                .studentName(attempt.getStudent().getName())
                .studentEmail(attempt.getStudent().getEmail())
                .studentPhone(attempt.getStudent().getPhone())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .totalMarks(attempt.getQuiz().getTotalMarks())
                .correctAnswers(correctCount)
                .totalQuestions(totalQ)
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}
