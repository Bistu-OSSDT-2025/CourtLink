<<<<<<< HEAD
package com.courtlink.admin.repository;

import com.courtlink.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Admin a WHERE a.isActive = true")
    java.util.List<Admin> findAllActiveAdmins();
    
    @Query("SELECT a FROM Admin a WHERE (a.username = ?1 OR a.email = ?1) AND a.isActive = true")
    Optional<Admin> findByUsernameOrEmailAndActive(String usernameOrEmail);
    
    @Query("SELECT a FROM Admin a WHERE a.username = ?1 OR a.email = ?2")
    Optional<Admin> findByUsernameOrEmail(String username, String email);
=======
package com.courtlink.admin.repository;

import com.courtlink.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 