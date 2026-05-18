package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "exam_type", nullable = false)
    private String examType; // e.g., "QUIZ" or "CODING"

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_phone")
    private String studentPhone;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
