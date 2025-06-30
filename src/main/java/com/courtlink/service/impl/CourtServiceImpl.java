package com.courtlink.service.impl;

import com.courtlink.dto.CourtRequest;
import com.courtlink.dto.CourtResponse;
import com.courtlink.entity.Court;
import com.courtlink.enums.CourtStatus;
import com.courtlink.repository.CourtRepository;
import com.courtlink.service.CourtService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtServiceImpl implements CourtService {
    
    private final CourtRepository courtRepository;

    @Autowired
    public CourtServiceImpl(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @Override
    @Transactional
    public CourtResponse createCourt(CourtRequest request) {
        Court court = new Court();
        BeanUtils.copyProperties(request, court);
        court = courtRepository.save(court);
        return convertToResponse(court);
    }

    @Override
    @Transactional
    public CourtResponse updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Court not found with id: " + id));
        
        BeanUtils.copyProperties(request, court, "id", "createdAt");
        court = courtRepository.save(court);
        return convertToResponse(court);
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new EntityNotFoundException("Court not found with id: " + id);
        }
        courtRepository.deleteById(id);
    }

    @Override
    public CourtResponse getCourt(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Court not found with id: " + id));
        return convertToResponse(court);
    }

    @Override
    public List<CourtResponse> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtResponse> searchCourts(String keyword, CourtStatus status) {
        List<Court> courts = courtRepository.searchCourts(keyword, status);
        return courts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourtResponse changeStatus(Long id, CourtStatus status) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Court not found with id: " + id));
        
        court.setStatus(status);
        court = courtRepository.save(court);
        return convertToResponse(court);
    }

    private CourtResponse convertToResponse(Court court) {
        CourtResponse response = new CourtResponse();
        BeanUtils.copyProperties(court, response);
        return response;
    }
} 