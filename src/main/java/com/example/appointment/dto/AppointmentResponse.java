package com.example.appointment.dto;

import com.example.appointment.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约响应对象
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "预约响应对象")
public class AppointmentResponse {

    @Schema(description = "预约ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "服务提供者ID", example = "provider456")
    private String providerId;

    @Schema(description = "服务类型", example = "篮球场")
    private String serviceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预约开始时间", example = "2024-03-25T10:00:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预约结束时间", example = "2024-03-25T12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "预约状态", example = "PENDING")
    private Appointment.AppointmentStatus status;

    @Schema(description = "预约金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "备注信息", example = "请准备篮球")
    private String notes;

    @Schema(description = "支付ID", example = "PAY123456789")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2024-03-21T10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2024-03-21T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从实体对象创建响应对象
     * 
     * @param appointment 预约实体
     * @return 预约响应对象
     */
    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setUserId(appointment.getUserId());
        response.setProviderId(appointment.getProviderId());
        response.setServiceType(appointment.getServiceType());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setStatus(appointment.getStatus());
        response.setAmount(appointment.getAmount());
        response.setNotes(appointment.getNotes());
        response.setPaymentId(appointment.getPaymentId());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
} 