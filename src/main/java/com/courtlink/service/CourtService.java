package com.courtlink.service;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.enums.CourtStatus;

import java.util.List;

public interface CourtService {
    CourtResponse createCourt(CourtRequest request);
    CourtResponse updateCourt(Long id, CourtRequest request);
    void deleteCourt(Long id);
    CourtResponse getCourt(Long id);
    List<CourtResponse> getAllCourts();
    
    // New features
    List<CourtResponse> searchCourts(String keyword, CourtStatus status);
    CourtResponse changeStatus(Long id, CourtStatus status);
} 