package com.example.quiz.entity;

import com.example.quiz.enums.SectionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assessment_sections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private SectionType sectionType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;
}
