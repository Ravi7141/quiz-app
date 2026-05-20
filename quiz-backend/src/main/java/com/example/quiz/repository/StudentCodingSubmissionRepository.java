package com.example.quiz.repository;

import com.example.quiz.entity.StudentCodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentCodingSubmissionRepository extends JpaRepository<StudentCodingSubmission, Long> {
    List<StudentCodingSubmission> findByAttemptId(Long attemptId);
    List<StudentCodingSubmission> findByAttemptIdAndCodingTestId(Long attemptId, Long codingTestId);
    List<StudentCodingSubmission> findByAssessmentAttemptId(Long assessmentAttemptId);
    List<StudentCodingSubmission> findByAssessmentAttemptIdAndCodingTestId(Long assessmentAttemptId, Long codingTestId);
    void deleteByAssessmentAttemptId(Long assessmentAttemptId);
}
