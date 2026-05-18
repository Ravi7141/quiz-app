package com.example.quiz.repository;

import com.example.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Question entity.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Fetch all questions belonging to a quiz */
    List<Question> findByQuizId(Long quizId);

    /** Count how many questions a quiz has */
    int countByQuizId(Long quizId);

    long countByQuizCreatedById(Long adminId);

    /** Delete all questions for a quiz */
    void deleteByQuizId(Long quizId);
}
