package com.example.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.appointment.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约数据传输对象
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "预约数据传输对象")
public class AppointmentDTO {

    @Schema(description = "预约ID")
    private Long id;

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private String userId;

    @NotBlank(message = "服务提供者ID不能为空")
    @Schema(description = "服务提供者ID", required = true)
    private String providerId;

    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型", required = true)
    private String serviceType;

    @NotNull(message = "预约开始时间不能为空")
    @Future(message = "预约开始时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预约开始时间", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    @Future(message = "预约结束时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预约结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "预约状态")
    private Appointment.AppointmentStatus status;

    @DecimalMin(value = "0.0", message = "金额不能为负数")
    @Schema(description = "预约金额")
    private BigDecimal amount;

    @Size(max = 500, message = "备注信息不能超过500字符")
    @Schema(description = "备注信息")
    private String notes;

    @Schema(description = "支付ID")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 转换为实体对象
     * 
     * @return 预约实体
     */
    public Appointment toEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(this.id);
        appointment.setUserId(this.userId);
        appointment.setProviderId(this.providerId);
        appointment.setServiceType(this.serviceType);
        appointment.setStartTime(this.startTime);
        appointment.setEndTime(this.endTime);
        appointment.setStatus(this.status != null ? this.status : Appointment.AppointmentStatus.PENDING);
        appointment.setAmount(this.amount);
        appointment.setNotes(this.notes);
        appointment.setPaymentId(this.paymentId);
        return appointment;
    }

    /**
     * 从实体对象创建DTO
     * 
     * @param appointment 预约实体
     * @return 预约DTO
     */
    public static AppointmentDTO fromEntity(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setProviderId(appointment.getProviderId());
        dto.setServiceType(appointment.getServiceType());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus());
        dto.setAmount(appointment.getAmount());
        dto.setNotes(appointment.getNotes());
        dto.setPaymentId(appointment.getPaymentId());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
} 