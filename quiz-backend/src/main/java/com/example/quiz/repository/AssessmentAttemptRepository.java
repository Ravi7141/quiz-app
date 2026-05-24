package com.example.quiz.repository;

import com.example.quiz.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    List<AssessmentAttempt> findByStudentId(Long studentId);
    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);
    Optional<AssessmentAttempt> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
    org.springframework.data.domain.Page<AssessmentAttempt> findByAssessmentCreatedById(Long createdById, org.springframework.data.domain.Pageable pageable);
}
