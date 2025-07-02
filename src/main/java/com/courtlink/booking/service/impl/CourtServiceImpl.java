package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Court;
import com.courtlink.booking.repository.CourtRepository;
import com.courtlink.booking.service.CourtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;

    @Override
    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    @Override
    public Court getCourtById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("场地不存在"));
    }

    @Override
    public List<Court> getAvailableCourts() {
        return courtRepository.findByAvailableTrue();
    }
} 