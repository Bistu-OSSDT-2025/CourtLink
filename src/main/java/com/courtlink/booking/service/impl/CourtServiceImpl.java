package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Court;
import com.courtlink.booking.entity.CourtTimeSlot;
import com.courtlink.booking.repository.CourtRepository;
import com.courtlink.booking.repository.CourtTimeSlotRepository;
import com.courtlink.booking.service.CourtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtTimeSlotRepository timeSlotRepository;

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

    @Override
    public List<Court> getCourtsWithTimeSlots(LocalDate date) {
        // 获取所有可用场地
        List<Court> courts = courtRepository.findByAvailableTrue();
        
        // 为每个场地加载对应日期的时间段
        courts.forEach(court -> {
            List<CourtTimeSlot> timeSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court.getId(), date);
            court.setTimeSlots(timeSlots);
        });
        
        return courts;
    }
} 