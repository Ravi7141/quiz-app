package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "passing_percentage")
    private Integer passingPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }
}
