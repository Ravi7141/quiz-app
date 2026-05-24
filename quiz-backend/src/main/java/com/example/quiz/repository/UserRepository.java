package com.example.quiz.repository;

import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find user by email for login/authentication */
    Optional<User> findByEmail(String email);

    /** Check if email already registered */
    boolean existsByEmail(String email);

    /** Find all users by role — used by admin to list students */
    List<User> findByRole(Role role);

    List<User> findByRoleAndCreatedById(Role role, Long createdById);

    org.springframework.data.domain.Page<User> findByRole(Role role, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByRoleAndCreatedById(Role role, Long createdById, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR u.phone LIKE CONCAT('%', :search, '%'))")
    org.springframework.data.domain.Page<User> findByRoleAndSearch(@org.springframework.data.repository.query.Param("role") Role role, @org.springframework.data.repository.query.Param("search") String search, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role = :role AND u.createdBy.id = :createdById AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR u.phone LIKE CONCAT('%', :search, '%'))")
    org.springframework.data.domain.Page<User> findByRoleAndCreatedByIdAndSearch(@org.springframework.data.repository.query.Param("role") Role role, @org.springframework.data.repository.query.Param("createdById") Long createdById, @org.springframework.data.repository.query.Param("search") String search, org.springframework.data.domain.Pageable pageable);

    /** Count users by role — used for admin dashboard stats */
    long countByRole(Role role);

    long countByRoleAndCreatedById(Role role, Long createdById);
}
