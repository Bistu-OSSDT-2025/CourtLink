package com.courtlink.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Table(name = "courts")
@NoArgsConstructor
@AllArgsConstructor
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String location;

    @Column
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(name = "price_per_hour")
    private Double pricePerHour;

    @Column
    private String facilities;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "maintenance_schedule")
    private String maintenanceSchedule;

    @Column(name = "is_active")
    private Boolean isActive = true;
} 