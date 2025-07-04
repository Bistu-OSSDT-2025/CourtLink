package com.courtlink.court.service;

import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.dto.CourtBatchRequest;
import com.courtlink.court.dto.CourtManagementDTO;
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
    
    List<CourtDTO> createMultipleCourts(CourtBatchRequest request);
    void deleteMultipleCourts(List<Long> courtIds);
    
    List<CourtDTO> batchUpdateStatus(List<Long> courtIds, CourtStatus status);
    List<CourtDTO> batchToggleEnabled(List<Long> courtIds);
    
    CourtManagementDTO getCourtManagementInfo();
    List<CourtDTO> getCourtsByLocation(Court.CourtLocation location);
    List<CourtDTO> getAvailableCourts();
    
    CourtDTO setMaintenanceMode(Long id, String reason);
    CourtDTO endMaintenanceMode(Long id);
    List<CourtDTO> getCourtsInMaintenance();
} 