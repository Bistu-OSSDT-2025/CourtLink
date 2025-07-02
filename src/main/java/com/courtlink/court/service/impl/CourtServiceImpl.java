package com.courtlink.court.service.impl;

import com.courtlink.admin.service.AdminService;
import com.courtlink.court.dto.CourtDTO;
import com.courtlink.court.dto.CourtRequest;
import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.repository.CourtRepository;
import com.courtlink.court.service.CourtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final AdminService adminService;

    @Override
    @Transactional
    public CourtDTO createCourt(CourtRequest request) {
        Court court = new Court();
        updateCourtFromRequest(court, request);
        court.setCreatedAt(LocalDateTime.now());
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    @Transactional
    public CourtDTO updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        updateCourtFromRequest(court, request);
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    public CourtDTO getCourtById(Long id) {
        return courtRepository.findById(id)
                .map(CourtDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
    }

    @Override
    public List<CourtDTO> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtDTO> getCourtsByStatus(CourtStatus status) {
        return courtRepository.findByStatus(status).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtDTO> getCourtsByType(Court.CourtType courtType) {
        return courtRepository.findByCourtType(courtType).stream()
                .map(CourtDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        courtRepository.delete(court);
    }

    @Override
    @Transactional
    public CourtDTO updateCourtStatus(Long id, CourtStatus status) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        court.setStatus(status);
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    @Override
    @Transactional
    public CourtDTO toggleCourtEnabled(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在: " + id));
        
        court.setEnabled(!court.isEnabled());
        court.setLastModifiedBy(adminService.getCurrentAdmin().getUsername());
        return CourtDTO.fromEntity(courtRepository.save(court));
    }

    private void updateCourtFromRequest(Court court, CourtRequest request) {
        court.setName(request.getName());
        court.setCourtType(request.getCourtType());
        court.setDescription(request.getDescription());
        court.setPricePerHour(request.getPricePerHour());
        court.setLocation(request.getLocation());
        court.setFacilities(request.getFacilities());
        court.setRules(request.getRules());
    }
} 