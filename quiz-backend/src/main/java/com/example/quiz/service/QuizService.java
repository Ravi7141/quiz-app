package com.example.quiz.service;

import com.example.quiz.dto.request.QuizRequest;
import com.example.quiz.dto.response.QuizResponse;
import com.example.quiz.dto.response.AssessmentResponse;
import com.example.quiz.dto.response.QuestionResponse;
import com.example.quiz.dto.response.CodingTestResponse;
import com.example.quiz.entity.Quiz;
import com.example.quiz.entity.QuizAttempt;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.entity.User;
import com.example.quiz.repository.QuestionRepository;
import com.example.quiz.repository.QuizAttemptRepository;
import com.example.quiz.repository.QuizRepository;
import com.example.quiz.repository.StudentAnswerRepository;
import com.example.quiz.repository.CodingTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Quiz Service — handles all quiz CRUD operations.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final CodingTestRepository codingTestRepository;
    private final AuthService authService;

    // ─── Create Quiz ──────────────────────────────────────────────────────────

    public QuizResponse createQuiz(QuizRequest request) {
        User currentAdmin = authService.getCurrentUser();
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .totalMarks(request.getTotalMarks())
                .active(request.getActive() != null ? request.getActive() : true)
                .scheduledFor(request.getScheduledFor())
                .validUntil(request.getValidUntil())
                .createdBy(currentAdmin)
                .build();
        return mapToResponse(quizRepository.save(quiz));
    }

    // ─── Get All Active Quizzes (Students) ────────────────────────────────────

    public List<QuizResponse> getActiveQuizzes() {
        User currentStudent = authService.getCurrentUser();
        List<Quiz> quizzes;
        if (currentStudent != null && currentStudent.getCreatedBy() != null) {
            quizzes = quizRepository.findByActiveTrueAndCreatedById(currentStudent.getCreatedBy().getId());
        } else {
            quizzes = quizRepository.findByActiveTrue();
        }
        return quizzes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get All Quizzes (Admin) ──────────────────────────────────────────────

    public List<QuizResponse> getAllQuizzes() {
        User currentAdmin = authService.getCurrentUser();
        List<Quiz> quizzes;
        if (currentAdmin != null) {
            quizzes = quizRepository.findByCreatedById(currentAdmin.getId());
        } else {
            quizzes = quizRepository.findAll();
        }
        return quizzes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Quiz By ID ───────────────────────────────────────────────────────

    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        return mapToResponse(quiz);
    }

    // ─── Update Quiz ──────────────────────────────────────────────────────────

    /**
     * Update an existing quiz.
     *
     * Steps:
     *  1. Find the quiz → throw if not found.
     *  2. Update only the fields provided in the request.
     *  3. Save and return updated QuizResponse.
     */
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));

        if (request.getTitle() != null)           quiz.setTitle(request.getTitle());
        if (request.getDescription() != null)     quiz.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) quiz.setDurationMinutes(request.getDurationMinutes());
        if (request.getTotalMarks() != null)      quiz.setTotalMarks(request.getTotalMarks());
        if (request.getActive() != null)          quiz.setActive(request.getActive());
        if (request.getScheduledFor() != null)    quiz.setScheduledFor(request.getScheduledFor());
        if (request.getValidUntil() != null)      quiz.setValidUntil(request.getValidUntil());

        return mapToResponse(quizRepository.save(quiz));
    }

    // ─── Delete Quiz ──────────────────────────────────────────────────────────

    /**
     * Delete a quiz by ID — cascades in order:
     *   student_answers → quiz_attempts → questions → quiz
     */
    @Transactional
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz", id);
        }
        // 1. Delete all student answers for every attempt of this quiz
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(id);
        for (QuizAttempt attempt : attempts) {
            studentAnswerRepository.deleteByAttemptId(attempt.getId());
        }
        // 2. Delete all attempts for this quiz
        quizAttemptRepository.deleteByQuizId(id);
        // 3. Delete all questions for this quiz
        questionRepository.deleteByQuizId(id);
        // 4. Delete the quiz itself
        quizRepository.deleteById(id);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private QuizResponse mapToResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .durationMinutes(quiz.getDurationMinutes())
                .totalMarks(quiz.getTotalMarks())
                .active(quiz.getActive())
                .scheduledFor(quiz.getScheduledFor())
                .validUntil(quiz.getValidUntil())
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    public AssessmentResponse getAssessment(Long examId) {
        Quiz quiz = quizRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", examId));

        return AssessmentResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .durationMinutes(quiz.getDurationMinutes())
                .active(quiz.getActive())
                .build();
    }
}
