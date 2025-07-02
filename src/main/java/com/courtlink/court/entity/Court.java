package com.courtlink.court.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.courtlink.booking.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courts")
@Data
@NoArgsConstructor
public class Court {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "court_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CourtType courtType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CourtStatus status = CourtStatus.AVAILABLE;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean available = true;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @Column(name = "max_players")
    private Integer maxPlayers;

    @Column(name = "has_lighting")
    private Boolean hasLighting = false;

    @Column(name = "indoor_outdoor", nullable = false)
    @Enumerated(EnumType.STRING)
    private CourtLocation courtLocation;

    @Column(nullable = false)
    private String location; // 场地位置
    private String facilities; // 场地设施描述
    private String rules; // 使用规则

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;

    @Column(name = "operating_hours", nullable = false)
    private Integer operatingHours = 14; // 默认营业时间为14小时（如：8:00-22:00）

    // 维护相关字段
    @Column(name = "maintenance_reason", length = 500)
    private String maintenanceReason; // 维护原因
    
    @Column(name = "maintenance_start_time")
    private LocalDateTime maintenanceStartTime; // 维护开始时间
    
    @Column(name = "maintenance_end_time")
    private LocalDateTime maintenanceEndTime; // 预计维护结束时间
    
    @Column(name = "maintenance_by", length = 100)
    private String maintenanceBy; // 维护负责人

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPricePerHour() {
        return this.pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setCourt(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setCourt(null);
    }

    public CourtStatus getStatus() {
        return this.status;
    }

    public void setStatus(CourtStatus status) {
        this.status = status;
    }

    public enum CourtType {
        TENNIS, BASKETBALL, BADMINTON, VOLLEYBALL, FOOTBALL, OTHER
    }

    public enum CourtLocation {
        INDOOR, OUTDOOR
    }
} 