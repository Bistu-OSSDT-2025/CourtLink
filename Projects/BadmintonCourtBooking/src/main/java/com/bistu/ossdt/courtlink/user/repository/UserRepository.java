package com.bistu.ossdt.courtlink.user.repository;

import com.bistu.ossdt.courtlink.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
} 