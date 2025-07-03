package com.courtlink.booking.service;

import com.courtlink.booking.entity.Court;
import java.time.LocalDate;
import java.util.List;

public interface CourtService {
    List<Court> getAllCourts();
    Court getCourtById(Long id);
    List<Court> getAvailableCourts();
    List<Court> getCourtsWithTimeSlots(LocalDate date);
} 