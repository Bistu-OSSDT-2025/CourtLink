package com.courtlink.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ԤԼ�����������?
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "ԤԼ��������")
public class AppointmentRequest {

    @NotBlank(message = "�û�ID����Ϊ��")
    @Schema(description = "�û�ID", required = true, example = "user123")
    private String userId;

    @NotBlank(message = "�����ṩ��ID����Ϊ��")
    @Schema(description = "�����ṩ��ID", required = true, example = "provider456")
    private String providerId;

    @NotBlank(message = "�������Ͳ���Ϊ��")
    @Schema(description = "��������", required = true, example = "����")
    private String serviceType;

    @NotNull(message = "ԤԼ��ʼʱ�䲻��Ϊ��")
    @Future(message = "ԤԼ��ʼʱ�������δ��ʱ��?)
    @Schema(description = "ԤԼ��ʼʱ��", required = true, example = "2024-03-25T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "ԤԼ����ʱ�䲻��Ϊ��")
    @Future(message = "ԤԼ����ʱ�������δ��ʱ��?)
    @Schema(description = "ԤԼ����ʱ��", required = true, example = "2024-03-25T12:00:00")
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "����Ϊ����?)
    @Schema(description = "ԤԼ���?, example = "100.00")
    private BigDecimal amount;

    @Size(max = 500, message = "��ע��Ϣ���ܳ���500�ַ�")
    @Schema(description = "��ע��Ϣ", example = "��׼������")
    private String notes;
} 
