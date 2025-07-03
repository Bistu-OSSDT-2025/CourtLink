package com.courtlink.booking.service.impl;

import com.courtlink.booking.entity.Court;
import com.courtlink.booking.repository.CourtRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CourtInitializationService {

    private final CourtRepository courtRepository;

    @PostConstruct
    public void initializeCourts() {
        if (courtRepository.count() == 0) {
            Court court1 = new Court();
            court1.setName("羽毛球场地1");
            court1.setDescription("标准羽毛球场地，适合双打比赛");
            court1.setPricePerHour(new BigDecimal("50.00"));
            court1.setAvailable(true);

            Court court2 = new Court();
            court2.setName("羽毛球场地2");
            court2.setDescription("标准羽毛球场地，适合训练");
            court2.setPricePerHour(new BigDecimal("45.00"));
            court2.setAvailable(true);

            Court court3 = new Court();
            court3.setName("羽毛球场地3");
            court3.setDescription("标准羽毛球场地，带空调");
            court3.setPricePerHour(new BigDecimal("60.00"));
            court3.setAvailable(true);

            courtRepository.saveAll(Arrays.asList(court1, court2, court3));
        }
    }
} 