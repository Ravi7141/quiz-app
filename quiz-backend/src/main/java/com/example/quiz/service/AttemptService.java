package com.example.quiz.service;

import com.example.quiz.dto.request.SubmitAnswerRequest;
import com.example.quiz.dto.response.AttemptResponse;
import com.example.quiz.entity.*;
import com.example.quiz.enums.AttemptStatus;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Phase 4 — Quiz Attempt Service (No Security)
 *
 * Handles:
 *  - POST /student/quizzes/start/{quizId}?studentId=1  → startAttempt()
 *  - POST /student/quizzes/submit-answer               → submitAnswer()
 *  - POST /student/quizzes/submit/{attemptId}          → submitAttempt()
 */
@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final StudentAnswerRepository answerRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    // ─── Start Attempt ────────────────────────────────────────────────────────

    /**
     * Start a new quiz attempt for a student.
     *
     * Steps:
     *  1. Find the student by ID (throw if not found).
     *  2. Find the quiz by ID (throw if not found).
     *  3. Check for an existing IN_PROGRESS attempt → throw BadRequest.
     *  4. Create and save a new QuizAttempt with status IN_PROGRESS.
     *  5. Return AttemptResponse.
     *
     * @param quizId    ID of the quiz to attempt
     * @param studentId ID returned from /login
     */
    public AttemptResponse startAttempt(Long quizId, Long studentId) {

        // Step 1 — Verify student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Step 2 — Verify quiz exists
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        // Step 3 — If already in-progress, return existing attempt (handles React double-mount in dev)
        Optional<QuizAttempt> existing = attemptRepository.findByStudentIdAndQuizIdAndStatus(
                studentId, quizId, AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        // Step 3b — Enforce attempt limit: max 2 completed attempts per quiz
        long completedCount = attemptRepository.countByStudentIdAndQuizIdAndStatus(
                studentId, quizId, AttemptStatus.SUBMITTED);
        if (completedCount >= 2) {
            throw new BadRequestException(
                    "ATTEMPT_LIMIT_REACHED: You have used all 2 attempts for this quiz.");
        }

        // Step 4 — Create attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .student(student)
                .quiz(quiz)
                .status(AttemptStatus.IN_PROGRESS)
                .build();

        QuizAttempt saved = attemptRepository.save(attempt);

        // Step 5 — Return response
        return buildResponse(saved);
    }

    // ─── Submit Single Answer ─────────────────────────────────────────────────

    /**
     * Save (or update) a student's answer to one question.
     *
     * Steps:
     *  1. Fetch the attempt → must be IN_PROGRESS.
     *  2. Fetch the question → must belong to the same quiz.
     *  3. Upsert the StudentAnswer (update if already answered).
     *  4. Mark isCorrect.
     */
    @Transactional
    public void submitAnswer(SubmitAnswerRequest request) {

        // Step 1
        QuizAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", request.getAttemptId()));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This attempt is already submitted. You cannot change answers.");
        }

        // Step 2
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", request.getQuestionId()));

        if (!question.getQuiz().getId().equals(attempt.getQuiz().getId())) {
            throw new BadRequestException("Question does not belong to this quiz.");
        }

        // Step 3 & 4 — Upsert answer
        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(request.getSelectedOption());

        StudentAnswer answer = answerRepository
                .findByAttemptIdAndQuestionId(request.getAttemptId(), request.getQuestionId())
                .orElse(StudentAnswer.builder()
                        .attempt(attempt)
                        .question(question)
                        .build());

        answer.setSelectedOption(request.getSelectedOption());
        answer.setIsCorrect(isCorrect);

        answerRepository.save(answer);
    }

    // ─── Submit Attempt ───────────────────────────────────────────────────────

    /**
     * Finalize the attempt and calculate the total score.
     *
     * Steps:
     *  1. Fetch the attempt → must be IN_PROGRESS.
     *  2. Fetch all student answers for this attempt.
     *  3. Calculate score = sum of marks for each correct answer.
     *  4. Update: status = SUBMITTED, score, submittedAt.
     *  5. Return AttemptResponse.
     */
    @Transactional
    public AttemptResponse submitAttempt(Long attemptId) {

        // Step 1
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new BadRequestException("Attempt already submitted.");
        }

        // Step 2
        List<StudentAnswer> answers = answerRepository.findByAttemptId(attemptId);

        // Step 3 — Calculate score (capped at quiz total marks)
        int rawScore = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .mapToInt(a -> a.getQuestion().getMarks())
                .sum();
        int totalMarks = attempt.getQuiz().getTotalMarks() != null ? attempt.getQuiz().getTotalMarks() : rawScore;
        int score = Math.min(rawScore, totalMarks);

        // Step 4 — Finalize
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setScore(score);
        attempt.setSubmittedAt(LocalDateTime.now());

        QuizAttempt saved = attemptRepository.save(attempt);

        // Step 5
        return buildResponse(saved);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private AttemptResponse buildResponse(QuizAttempt attempt) {
        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .status(attempt.getStatus())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }
}
