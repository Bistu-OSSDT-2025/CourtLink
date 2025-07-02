package com.courtlink.court.service.impl;

import com.courtlink.court.entity.Court;
import com.courtlink.court.entity.CourtStatus;
import com.courtlink.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Order(2) // 在管理员初始化之后运行
public class CourtInitializationService implements CommandLineRunner {

    private final CourtRepository courtRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courtRepository.count() == 0) {
            initializeCourts();
        }
    }

    private void initializeCourts() {
        List<Court> defaultCourts = Arrays.asList(
            createCourt(
                "羽毛球场A",
                Court.CourtType.BADMINTON,
                "标准羽毛球场",
                new BigDecimal("100.00"),
                "一楼东侧",
                "标准尺寸，专业照明，更衣室，储物柜",
                "1. 请穿着专业运动鞋\n2. 禁止在场地内吸烟\n3. 请保持场地整洁"
            ),
            createCourt(
                "羽毛球场B",
                Court.CourtType.BADMINTON,
                "标准羽毛球场",
                new BigDecimal("100.00"),
                "一楼西侧",
                "标准尺寸，自然采光，更衣室，饮水机",
                "1. 请穿着专业运动鞋\n2. 禁止在场地内吸烟\n3. 请保持场地整洁"
            ),
            createCourt(
                "羽毛球场C",
                Court.CourtType.BADMINTON,
                "多功能羽毛球场",
                new BigDecimal("120.00"),
                "二楼中央",
                "标准尺寸，专业照明，观众席，计分板，更衣室",
                "1. 请穿着专业运动鞋\n2. 禁止在场地内吸烟\n3. 请保持场地整洁\n4. 比赛时段需提前预约"
            ),
            createCourt(
                "羽毛球场D",
                Court.CourtType.BADMINTON,
                "初学者羽毛球场",
                new BigDecimal("80.00"),
                "二楼东侧",
                "标准尺寸，基础照明，更衣室",
                "1. 请穿着运动鞋\n2. 禁止在场地内吸烟\n3. 请保持场地整洁"
            )
        );

        courtRepository.saveAll(defaultCourts);
        log.info("初始化了 {} 个场地", defaultCourts.size());
    }

    private Court createCourt(
            String name,
            Court.CourtType courtType,
            String description,
            BigDecimal pricePerHour,
            String location,
            String facilities,
            String rules
    ) {
        Court court = new Court();
        court.setName(name);
        court.setCourtType(courtType);
        court.setDescription(description);
        court.setPricePerHour(pricePerHour);
        court.setLocation(location);
        court.setFacilities(facilities);
        court.setRules(rules);
        court.setStatus(CourtStatus.AVAILABLE);
        court.setEnabled(true);
        court.setCreatedAt(LocalDateTime.now());
        court.setLastModifiedAt(LocalDateTime.now());
        court.setLastModifiedBy("system");
        return court;
    }
} 