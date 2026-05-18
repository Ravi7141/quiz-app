package com.example.quiz.repository;

import com.example.quiz.entity.CodingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CodingTest entity.
 */
@Repository
public interface CodingTestRepository extends JpaRepository<CodingTest, Long> {
    List<CodingTest> findByCreatedById(Long createdById);
    long countByCreatedById(Long createdById);
}
