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

    /** Count users by role — used for admin dashboard stats */
    long countByRole(Role role);

    long countByRoleAndCreatedById(Role role, Long createdById);
}
