package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a coding problem available to students.
 *
 * Table: coding_tests
 */
@Entity
@Table(name = "coding_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Problem title */
    @Column(nullable = false)
    private String title;

    /** Full problem description */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Sample input visible to the student */
    @Column(name = "sample_input", columnDefinition = "TEXT")
    private String sampleInput;

    /** Expected output for sample input */
    @Column(name = "sample_output", columnDefinition = "TEXT")
    private String sampleOutput;

    /** Difficulty level: EASY, MEDIUM, HARD */
    @Column(nullable = false)
    private String difficulty;

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
    }
}
