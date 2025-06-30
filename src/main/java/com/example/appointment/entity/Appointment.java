package com.example.appointment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约实体类
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "appointments")
@Schema(description = "预约信息")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "预约ID")
    private Long id;

    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "用户ID")
    private String userId;

    @NotBlank(message = "服务提供者ID不能为空")
    @Column(name = "provider_id", nullable = false)
    @Schema(description = "服务提供者ID")
    private String providerId;

    @NotBlank(message = "服务类型不能为空")
    @Column(name = "service_type", nullable = false)
    @Schema(description = "服务类型")
    private String serviceType;

    @NotNull(message = "预约开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time", nullable = false)
    @Schema(description = "预约开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time", nullable = false)
    @Schema(description = "预约结束时间")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "预约状态")
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @DecimalMin(value = "0.0", message = "金额不能为负数")
    @Column(name = "amount", precision = 10, scale = 2)
    @Schema(description = "预约金额")
    private BigDecimal amount;

    @Size(max = 500, message = "备注信息不能超过500字符")
    @Column(name = "notes", length = 500)
    @Schema(description = "备注信息")
    private String notes;

    @Column(name = "payment_id")
    @Schema(description = "支付ID")
    private String paymentId;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(description = "版本号")
    private Long version;

    /**
     * 预约状态枚举
     */
    public enum AppointmentStatus {
        PENDING("待确认"),
        CONFIRMED("已确认"),
        CANCELLED("已取消"),
        COMPLETED("已完成"),
        EXPIRED("已过期");

        private final String description;

        AppointmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 