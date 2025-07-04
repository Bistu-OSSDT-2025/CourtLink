package com.courtlink.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "courts")
@Getter
@Setter
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    private boolean available = true;

    // 临时字段，用于返回时间段信息（不持久化到数据库）
    @Transient
    private List<CourtTimeSlot> timeSlots;
} 