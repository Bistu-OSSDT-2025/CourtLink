package com.courtlink.booking.repository;

import com.courtlink.booking.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByAvailableTrue();
    long countByAvailable(boolean available);
} 