package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores the answer a student submitted for a specific question
 * during a quiz attempt.
 *
 * Table: student_answers
 */
@Entity
@Table(name = "student_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The attempt this answer belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    /** The question being answered */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Answer submitted by student: A, B, C, or D */
    @Column(name = "selected_option")
    private String selectedOption;

    /** True if selectedOption equals question.correctAnswer */
    @Column(name = "is_correct")
    private Boolean isCorrect;
}
