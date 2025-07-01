package com.courtlink.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.courtlink.booking.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 棰勭害鏁版嵁浼犺緭瀵硅�?
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "棰勭害鏁版嵁浼犺緭瀵硅�?)
public class AppointmentDTO {

    @Schema(description = "棰勭害ID")
    private Long id;

    @NotBlank(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", required = true)
    private String userId;

    @NotBlank(message = "鏈嶅姟鎻愪緵鑰匢D涓嶈兘涓虹┖")
    @Schema(description = "鏈嶅姟鎻愪緵鑰匢D", required = true)
    private String providerId;

    @NotBlank(message = "鏈嶅姟绫诲瀷涓嶈兘涓虹�?)
    @Schema(description = "鏈嶅姟绫诲�?, required = true)
    private String serviceType;

    @NotNull(message = "棰勭害寮€濮嬫椂闂翠笉鑳戒负绌�")
    @Future(message = "棰勭害寮€濮嬫椂闂村繀椤绘槸鏈�鏉ユ椂闂�")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "棰勭害寮€濮嬫椂闂�", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "棰勭害缁撴潫鏃堕棿涓嶈兘涓虹�?)
    @Future(message = "棰勭害缁撴潫鏃堕棿蹇呴』鏄�鏈�鏉ユ椂闂�")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "棰勭害缁撴潫鏃堕�?, required = true)
    private LocalDateTime endTime;

    @Schema(description = "棰勭害鐘舵€�?)
    private Appointment.AppointmentStatus status;

    @DecimalMin(value = "0.0", message = "閲戦�濅笉鑳戒负璐熸�?)
    @Schema(description = "棰勭害閲戦��?)
    private BigDecimal amount;

    @Size(max = 500, message = "澶囨敞淇℃伅涓嶈兘瓒呰繃500瀛楃��")
    @Schema(description = "澶囨敞淇℃伅")
    private String notes;

    @Schema(description = "鏀�浠業D")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "鍒涘缓鏃堕棿")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "鏇存柊鏃堕棿")
    private LocalDateTime updatedAt;

    /**
     * 杞�鎹�涓哄疄浣撳�硅薄
     * 
     * @return 棰勭害瀹炰�?
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
     * 浠庡疄浣撳�硅薄鍒涘缓DTO
     * 
     * @param appointment 棰勭害瀹炰�?
     * @return 棰勭害DTO
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
