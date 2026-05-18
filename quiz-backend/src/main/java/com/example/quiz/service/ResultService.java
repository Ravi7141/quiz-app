package com.example.quiz.service;

import com.example.quiz.dto.response.ResultResponse;
import com.example.quiz.entity.QuizAttempt;
import com.example.quiz.enums.AttemptStatus;
import com.example.quiz.exception.BadRequestException;
import com.example.quiz.exception.ResourceNotFoundException;
import com.example.quiz.repository.QuizAttemptRepository;
import com.example.quiz.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Phase 5 — Result Service
 *
 * Handles:
 *  - GET /student/results/{attemptId} → getResult()
 */
@Service
@RequiredArgsConstructor
public class ResultService {

    private final QuizAttemptRepository attemptRepository;
    private final StudentAnswerRepository answerRepository;

    /**
     * Build a detailed result for a submitted attempt.
     *
     * Steps:
     *  1. Fetch the attempt → throw if not found.
     *  2. Verify the attempt is SUBMITTED (not IN_PROGRESS).
     *  3. Count total questions and correct answers.
     *  4. Calculate percentage.
     *  5. Determine pass/fail (>= 50% = pass).
     *  6. Return ResultResponse.
     */
    public ResultResponse getResult(Long attemptId) {

        // Step 1
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));

        // Step 2
        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            throw new BadRequestException("Quiz attempt has not been submitted yet.");
        }

        // Step 3
        int totalQuestions  = answerRepository.findByAttemptId(attemptId).size();
        int correctAnswers  = answerRepository.countByAttemptIdAndIsCorrectTrue(attemptId);
        int score           = attempt.getScore();
        int totalMarks      = attempt.getQuiz().getTotalMarks();

        // Step 4
        double percentage = totalMarks > 0 ? ((double) score / totalMarks) * 100 : 0;

        // Step 5 — pass if >= 50%
        boolean passed = percentage >= 50.0;

        // Step 6
        return ResultResponse.builder()
                .attemptId(attempt.getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .studentName(attempt.getStudent().getName())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .score(score)
                .totalMarks(totalMarks)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .passed(passed)
                .build();
    }
}
