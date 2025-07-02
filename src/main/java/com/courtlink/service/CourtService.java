package com.courtlink.service;

import com.courtlink.dto.CourtDTO;
import java.util.List;

public interface CourtService {
    List<CourtDTO> getAllCourts();
    CourtDTO getCourtById(Long id);
    CourtDTO createCourt(CourtDTO courtDTO);
    CourtDTO updateCourt(Long id, CourtDTO courtDTO);
    void deleteCourt(Long id);
    CourtDTO updateCourtStatus(Long id, String status);
    List<CourtDTO> searchCourts(String keyword, String status);
} 