package com.courtlink.entity;

import com.courtlink.enums.CourtStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;        // Court name

    @Column(nullable = false, length = 200)
    private String location;    // Location

    @Column(length = 500)
    private String description; // Description

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status; // Status: AVAILABLE / UNAVAILABLE / MAINTENANCE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Manual setter for status (in case Lombok doesn't work)
    public void setStatus(CourtStatus status) {
        this.status = status;
    }
} 