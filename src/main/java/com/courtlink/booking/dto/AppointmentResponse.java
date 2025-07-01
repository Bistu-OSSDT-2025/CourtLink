package com.courtlink.booking.dto;

import com.courtlink.booking.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ԤԼ��Ӧ����
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "ԤԼ��Ӧ����")
public class AppointmentResponse {

    @Schema(description = "ԤԼID", example = "1")
    private Long id;

    @Schema(description = "�û�ID", example = "user123")
    private String userId;

    @Schema(description = "�����ṩ��ID", example = "provider456")
    private String providerId;

    @Schema(description = "��������", example = "����")
    private String serviceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "ԤԼ��ʼʱ��", example = "2024-03-25T10:00:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "ԤԼ����ʱ��", example = "2024-03-25T12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "ԤԼ״̬", example = "PENDING")
    private Appointment.AppointmentStatus status;

    @Schema(description = "ԤԼ���?, example = "100.00")
    private BigDecimal amount;

    @Schema(description = "��ע��Ϣ", example = "��׼������")
    private String notes;

    @Schema(description = "֧��ID", example = "PAY123456789")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ��", example = "2024-03-21T10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ��", example = "2024-03-21T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * ��ʵ����󴴽���Ӧ����?
     * 
     * @param appointment ԤԼʵ��
     * @return ԤԼ��Ӧ����
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
