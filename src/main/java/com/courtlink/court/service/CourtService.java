package com.courtlink.court.service;

import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;

import java.util.List;

public interface CourtService {
    CourtDTO createCourt(CourtRequest request);
    CourtDTO updateCourt(Long id, CourtRequest request);
    CourtDTO getCourtById(Long id);
    List<CourtDTO> getAllCourts();
    List<CourtDTO> getCourtsByStatus(CourtStatus status);
    List<CourtDTO> getCourtsByType(Court.CourtType courtType);
    void deleteCourt(Long id);
    CourtDTO updateCourtStatus(Long id, CourtStatus status);
    CourtDTO toggleCourtEnabled(Long id);
} 