package com.example.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.appointment.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ԤԼ���ݴ������
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "ԤԼ���ݴ������")
public class AppointmentDTO {

    @Schema(description = "ԤԼID")
    private Long id;

    @NotBlank(message = "�û�ID����Ϊ��")
    @Schema(description = "�û�ID", required = true)
    private String userId;

    @NotBlank(message = "�����ṩ��ID����Ϊ��")
    @Schema(description = "�����ṩ��ID", required = true)
    private String providerId;

    @NotBlank(message = "�������Ͳ���Ϊ��")
    @Schema(description = "��������", required = true)
    private String serviceType;

    @NotNull(message = "ԤԼ��ʼʱ�䲻��Ϊ��")
    @Future(message = "ԤԼ��ʼʱ�������δ��ʱ��")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "ԤԼ��ʼʱ��", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "ԤԼ����ʱ�䲻��Ϊ��")
    @Future(message = "ԤԼ����ʱ�������δ��ʱ��")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "ԤԼ����ʱ��", required = true)
    private LocalDateTime endTime;

    @Schema(description = "ԤԼ״̬")
    private Appointment.AppointmentStatus status;

    @DecimalMin(value = "0.0", message = "����Ϊ����")
    @Schema(description = "ԤԼ���")
    private BigDecimal amount;

    @Size(max = 500, message = "��ע��Ϣ���ܳ���500�ַ�")
    @Schema(description = "��ע��Ϣ")
    private String notes;

    @Schema(description = "֧��ID")
    private String paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ��")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ��")
    private LocalDateTime updatedAt;

    /**
     * ת��Ϊʵ�����
     * 
     * @return ԤԼʵ��
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
     * ��ʵ����󴴽�DTO
     * 
     * @param appointment ԤԼʵ��
     * @return ԤԼDTO
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