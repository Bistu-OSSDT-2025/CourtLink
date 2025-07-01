package com.courtlink.booking.dto;

import com.courtlink.booking.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * ԤԼ��ѯ��������
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "ԤԼ��ѯ����")
public class AppointmentQuery {

    @Schema(description = "�û�ID", example = "user123")
    private String userId;

    @Schema(description = "�����ṩ��ID", example = "provider456")
    private String providerId;

    @Schema(description = "��������", example = "����")
    private String serviceType;

    @Schema(description = "ԤԼ״̬", example = "PENDING")
    private Appointment.AppointmentStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "��ʼʱ�䷶Χ-��ʼ", example = "2024-03-25T00:00:00")
    private LocalDateTime startTimeFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "��ʼʱ�䷶Χ-����", example = "2024-03-25T23:59:59")
    private LocalDateTime startTimeTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ�䷶Χ-��ʼ", example = "2024-03-25T00:00:00")
    private LocalDateTime endTimeFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "����ʱ�䷶Χ-����", example = "2024-03-25T23:59:59")
    private LocalDateTime endTimeTo;

    @Schema(description = "ҳ��", example = "0")
    private Integer page = 0;

    @Schema(description = "ÿҳ��С", example = "10")
    private Integer size = 10;

    @Schema(description = "�����ֶ�", example = "startTime")
    private String sortBy = "startTime";

    @Schema(description = "������", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
} 
