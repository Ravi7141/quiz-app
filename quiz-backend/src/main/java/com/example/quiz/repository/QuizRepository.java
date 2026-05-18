package com.example.quiz.repository;

import com.example.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Quiz entity.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /** Return only quizzes visible to students */
    List<Quiz> findByActiveTrue();

    List<Quiz> findByActiveTrueAndCreatedById(Long createdById);

    List<Quiz> findByCreatedById(Long createdById);

    long countByCreatedById(Long createdById);
}
