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
 * 支付实体类
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payments")
@Schema(description = "支付信息")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "支付ID")
    private Long id;

    @NotBlank(message = "支付订单号不能为空")
    @Column(name = "payment_no", nullable = false, unique = true)
    @Schema(description = "支付订单号")
    private String paymentNo;

    @NotBlank(message = "关联预约ID不能为空")
    @Column(name = "appointment_id", nullable = false)
    @Schema(description = "关联预约ID")
    private String appointmentId;

    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    @Schema(description = "用户ID")
    private String userId;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @Schema(description = "支付方式")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "支付状态")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id")
    @Schema(description = "第三方交易ID")
    private String transactionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "paid_at")
    @Schema(description = "支付完成时间")
    private LocalDateTime paidAt;

    @Size(max = 500, message = "支付备注不能超过500字符")
    @Column(name = "notes", length = 500)
    @Schema(description = "支付备注")
    private String notes;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "refunded_at")
    @Schema(description = "退款时间")
    private LocalDateTime refundedAt;

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
     * 支付方式枚举
     */
    public enum PaymentMethod {
        ALIPAY("支付宝"),
        WECHAT("微信支付"),
        BANK_CARD("银行卡"),
        CASH("现金"),
        MOCK("模拟支付");

        private final String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 支付状态枚举
     */
    public enum PaymentStatus {
        PENDING("待支付"),
        PROCESSING("处理中"),
        SUCCESS("支付成功"),
        FAILED("支付失败"),
        CANCELLED("已取消"),
        REFUNDED("已退款"),
        PARTIAL_REFUNDED("部分退款");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 