package com.example.quiz.entity;

import com.example.quiz.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a registered user (student or admin).
 *
 * Table: users
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the user */
    @Column(nullable = false)
    private String name;

    /** Unique email used for login */
    @Column(nullable = false, unique = true)
    private String email;

    /** Plain-text password (no encryption for now) */
    @Column(nullable = false)
    private String password;

    /** Phone number */
    @Column
    private String phone;

    /** STUDENT or ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Timestamp when the account was created */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
