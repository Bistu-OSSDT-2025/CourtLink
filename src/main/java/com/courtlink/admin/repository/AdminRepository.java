package com.courtlink.admin.repository;

import com.courtlink.admin.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // Find admin by username
    Optional<Admin> findByUsername(String username);
    
    // Find admin by email
    Optional<Admin> findByEmail(String email);
    
    // Find admin by username or email (for login)
    @Query("SELECT a FROM Admin a WHERE a.username = :usernameOrEmail OR a.email = :usernameOrEmail")
    Optional<Admin> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find admins by role
    List<Admin> findByRole(Admin.AdminRole role);
    
    // Find admins by status
    List<Admin> findByStatus(Admin.AdminStatus status);
    
    // Find active admins
    @Query("SELECT a FROM Admin a WHERE a.status = 'ACTIVE' AND a.isLocked = false")
    List<Admin> findActiveAdmins();
    
    // Find admins with pagination
    Page<Admin> findByRoleAndStatus(Admin.AdminRole role, Admin.AdminStatus status, Pageable pageable);
    
    // Find admins by name pattern
    @Query("SELECT a FROM Admin a WHERE a.fullName LIKE %:name% OR a.username LIKE %:name%")
    Page<Admin> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    // Find admins created after specific date
    List<Admin> findByCreatedAtAfter(LocalDateTime dateTime);
    
    // Find recently logged in admins
    @Query("SELECT a FROM Admin a WHERE a.lastLogin IS NOT NULL ORDER BY a.lastLogin DESC")
    List<Admin> findRecentlyLoggedInAdmins(Pageable pageable);
    
    // Find locked admins
    List<Admin> findByIsLockedTrue();
    
    // Statistics queries
    
    // Count admins by role
    @Query("SELECT a.role, COUNT(a) FROM Admin a GROUP BY a.role")
    List<Object[]> countByRole();
    
    // Count admins by status
    @Query("SELECT a.status, COUNT(a) FROM Admin a GROUP BY a.status")
    List<Object[]> countByStatus();
    
    // Count active admins
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status = 'ACTIVE' AND a.isLocked = false")
    Long countActiveAdmins();
    
    // Count locked admins
    Long countByIsLockedTrue();
    
    // Count admins created today
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.createdAt >= :startOfDay AND a.createdAt < :endOfDay")
    Long countTodayCreated(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
} 
