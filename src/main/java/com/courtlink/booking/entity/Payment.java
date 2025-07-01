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
 * ֧��ʵ����
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payments")
@Schema(description = "֧����Ϣ")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "֧��ID")
    private Long id;

    @NotBlank(message = "֧�������Ų���Ϊ��")
    @Column(name = "payment_no", nullable = false, unique = true)
    @Schema(description = "֧��������")
    private String paymentNo;

    @NotBlank(message = "����ԤԼID����Ϊ��")
    @Column(name = "appointment_id", nullable = false)
    @Schema(description = "����ԤԼID")
    private String appointmentId;

    @NotBlank(message = "�û�ID����Ϊ��")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "�û�ID")
    private String userId;

    @NotNull(message = "֧������Ϊ��?)
    @DecimalMin(value = "0.01", message = "֧�����������?")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @Schema(description = "֧�����?)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @Schema(description = "֧����ʽ")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "֧��״̬")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id")
    @Schema(description = "����������ID")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "paid_at")
    @Schema(description = "֧�����ʱ��?)
    private LocalDateTime paidAt;

    @Size(max = 500, message = "֧����ע���ܳ���500�ַ�")
    @Column(name = "notes", length = 500)
    @Schema(description = "֧����ע")
    private String notes;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Schema(description = "�˿���")
    private BigDecimal refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "refunded_at")
    @Schema(description = "�˿�ʱ��")
    private LocalDateTime refundedAt;

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
     * ֧����ʽö��
     */
    public enum PaymentMethod {
        ALIPAY("֧����"),
        WECHAT("΢��֧��"),
        BANK_CARD("���п�"),
        CASH("�ֽ�"),
        MOCK("ģ��֧��");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * ֧��״̬ö��
     */
    public enum PaymentStatus {
        PENDING("��֧��"),
        PROCESSING("������"),
        SUCCESS("֧���ɹ�"),
        FAILED("֧��ʧ��"),
        CANCELLED("��ȡ��"),
        REFUNDED("���˿�"),
        PARTIAL_REFUNDED("�����˿�");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 
