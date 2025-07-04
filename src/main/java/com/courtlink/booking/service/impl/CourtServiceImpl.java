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
<<<<<<< HEAD
import java.util.stream.Collectors;
=======
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e

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
        
<<<<<<< HEAD
        // 为每个场地加载对应日期的时间段，只返回开放且可用的时间段
        courts.forEach(court -> {
            List<CourtTimeSlot> timeSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court.getId(), date);
            // 过滤掉管理员关闭的时间段或不可用的时间段
            List<CourtTimeSlot> openTimeSlots = timeSlots.stream()
                .filter(slot -> slot.isOpen() && slot.isAvailable())
                .collect(Collectors.toList());
            court.setTimeSlots(openTimeSlots);
=======
        // 为每个场地加载对应日期的时间段
        courts.forEach(court -> {
            List<CourtTimeSlot> timeSlots = timeSlotRepository.findByCourtIdAndDateOrderByStartTime(court.getId(), date);
            court.setTimeSlots(timeSlots);
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
        });
        
        return courts;
    }
} 