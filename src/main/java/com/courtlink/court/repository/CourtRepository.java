package com.courtlink.court.repository;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByEnabled(boolean enabled);
    List<Court> findByStatus(CourtStatus status);
    List<Court> findByCourtType(Court.CourtType courtType);
    boolean existsByName(String name);
} 