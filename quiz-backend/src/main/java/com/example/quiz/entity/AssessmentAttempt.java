package com.example.quiz.entity;

import com.example.quiz.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private Integer violations;

    @PrePersist
    public void prePersist() {
        this.startedAt = LocalDateTime.now();
        if (this.status == null) this.status = AttemptStatus.IN_PROGRESS;
        if (this.violations == null) this.violations = 0;
    }
}
