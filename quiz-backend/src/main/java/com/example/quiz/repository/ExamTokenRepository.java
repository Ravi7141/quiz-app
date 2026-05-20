package com.example.quiz.repository;

import com.example.quiz.entity.ExamToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamTokenRepository extends JpaRepository<ExamToken, Long> {
    Optional<ExamToken> findByToken(String token);
    List<ExamToken> findByExamIdAndExamType(Long examId, String examType);
    List<ExamToken> findByStudentEmail(String studentEmail);
}
