package com.courtlink.repository;

import com.courtlink.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    @Query("SELECT c FROM Court c WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR LOWER(c.status) = LOWER(:status)) AND " +
           "c.isActive = true")
    List<Court> searchCourts(@Param("keyword") String keyword, @Param("status") String status);
} 