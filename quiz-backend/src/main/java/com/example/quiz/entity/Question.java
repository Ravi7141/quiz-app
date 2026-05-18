package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an MCQ question belonging to a quiz.
 *
 * Table: questions
 */
@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The question text */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    /** Option A */
    @Column(nullable = false)
    private String optionA;

    /** Option B */
    @Column(nullable = false)
    private String optionB;

    /** Option C */
    @Column(nullable = false)
    private String optionC;

    /** Option D */
    @Column(nullable = false)
    private String optionD;

    /** Correct answer: A, B, C, or D */
    @Column(nullable = false)
    private String correctAnswer;

    /** Marks awarded for correct answer */
    @Column(nullable = false)
    private Integer marks;

    /** The quiz this question belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
