package com.courtlink.booking.entity;

import com.courtlink.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "appointments")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourtTimeSlot> timeSlots;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum AppointmentStatus {
        PENDING,    // 待支付
        CONFIRMED,  // 预定成功，待支付
        CANCELLED,  // 已取消
        COMPLETED   // 已完成
    }

    // 计算预约时长（小时）
    public int getDurationHours() {
        return endTime.getHour() - startTime.getHour();
    }

    // 检查是否为连续时间段
    public boolean isContinuousTimeSlots() {
        if (timeSlots == null || timeSlots.size() <= 1) {
            return true;
        }
        
        // 按开始时间排序
        List<CourtTimeSlot> sortedSlots = timeSlots.stream()
            .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
            .toList();
        
        for (int i = 1; i < sortedSlots.size(); i++) {
            LocalTime prevEnd = sortedSlots.get(i - 1).getEndTime();
            LocalTime currentStart = sortedSlots.get(i).getStartTime();
            
            // 检查时间段是否相邻
            if (!prevEnd.equals(currentStart)) {
                return false;
            }
        }
        
        return true;
    }
} 