package com.courtlink.booking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ԤԼʵ����
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "appointments")
@Schema(description = "ԤԼ��Ϣ")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ԤԼID")
    private Long id;

    @NotBlank(message = "�û�ID����Ϊ��")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "�û�ID")
    private String userId;

    @NotBlank(message = "�����ṩ��ID����Ϊ��")
    @Column(name = "provider_id", nullable = false)
    @Schema(description = "�����ṩ��ID")
    private String providerId;

    @NotBlank(message = "�������Ͳ���Ϊ��")
    @Column(name = "service_type", nullable = false)
    @Schema(description = "��������")
    private String serviceType;

    @NotNull(message = "ԤԼ��ʼʱ�䲻��Ϊ��")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "start_time", nullable = false)
    @Schema(description = "ԤԼ��ʼʱ��")
    private LocalDateTime startTime;

    @NotNull(message = "ԤԼ����ʱ�䲻��Ϊ��")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "end_time", nullable = false)
    @Schema(description = "ԤԼ����ʱ��")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "ԤԼ״̬")
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @DecimalMin(value = "0.0", message = "����Ϊ����?)
    @Column(name = "amount", precision = 10, scale = 2)
    @Schema(description = "ԤԼ���?)
    private BigDecimal amount;

    @Size(max = 500, message = "��ע��Ϣ���ܳ���500�ַ�")
    @Column(name = "notes", length = 500)
    @Schema(description = "��ע��Ϣ")
    private String notes;

    @Column(name = "payment_id")
    @Schema(description = "֧��ID")
    private String paymentId;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "����ʱ��")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "����ʱ��")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Schema(description = "�汾��")
    private Long version;

    /**
     * ԤԼ״̬ö��
     */
    public enum AppointmentStatus {
        PENDING("��ȷ��"),
        CONFIRMED("��ȷ��"),
        CANCELLED("��ȡ��"),
        COMPLETED("�����?),
        EXPIRED("�ѹ���");

        private final String description;

        AppointmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 
