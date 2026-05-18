package com.example.quiz.repository;

import com.example.quiz.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentAnswer entity.
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /** All answers submitted during a specific attempt */
    List<StudentAnswer> findByAttemptId(Long attemptId);

    /** Delete all answers for a specific attempt */
    void deleteByAttemptId(Long attemptId);

    /** Check if student already answered this question in this attempt */
    Optional<StudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    /** Count how many correct answers in an attempt */
    int countByAttemptIdAndIsCorrectTrue(Long attemptId);
}
