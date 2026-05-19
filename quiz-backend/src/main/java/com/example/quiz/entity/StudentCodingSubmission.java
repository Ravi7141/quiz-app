package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_coding_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCodingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = true)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_attempt_id", nullable = true)
    private AssessmentAttempt assessmentAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coding_test_id", nullable = false)
    private CodingTest codingTest;

    @Column(columnDefinition = "TEXT")
    private String code;

    private String language;

    private Boolean passed;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }
}
