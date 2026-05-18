package com.example.quiz.repository;

import com.example.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for QuizAttempt entity.
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    /** Find all attempts by a student */
    List<QuizAttempt> findByStudentId(Long studentId);

    /** Find all attempts for a quiz */
    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findByQuizCreatedById(Long adminId);

    long countByQuizCreatedById(Long adminId);

    /** Delete all attempts for a quiz */
    void deleteByQuizId(Long quizId);

    /** Count completed attempts for a student on a specific quiz */
    long countByStudentIdAndQuizIdAndStatus(
            Long studentId,
            Long quizId,
            com.example.quiz.enums.AttemptStatus status
    );

    /** Check if student already has an in-progress attempt for a quiz */
    Optional<QuizAttempt> findByStudentIdAndQuizIdAndStatus(
            Long studentId,
            Long quizId,
            com.example.quiz.enums.AttemptStatus status
    );
}
