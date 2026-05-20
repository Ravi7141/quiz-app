package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a quiz created by an admin.
 *
 * Table: quizzes
 */
@Entity
@Table(name = "exams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quiz title, e.g. "Java Fundamentals" */
    @Column(nullable = false)
    private String title;

    /** Short description */
    @Column(length = 1000)
    private String description;

    /** Duration in minutes */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** Total marks for the quiz */
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    /** Whether this quiz is visible to students */
    @Column(nullable = false)
    private Boolean active;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }
}
