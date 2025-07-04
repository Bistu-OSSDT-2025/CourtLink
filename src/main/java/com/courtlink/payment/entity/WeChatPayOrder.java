package com.courtlink.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wechat_pay_orders")
@EqualsAndHashCode(callSuper = false)
public class WeChatPayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商户订单号（系统内部订单号）
     */
    @Column(name = "out_trade_no", unique = true, nullable = false)
    private String outTradeNo;

    /**
     * 微信支付订单号
     */
    @Column(name = "transaction_id")
    private String transactionId;

    /**
     * 关联的支付记录ID
     */
    @Column(name = "payment_id")
    private Long paymentId;

    /**
     * 商品描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 支付金额（分）
     */
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    /**
     * 支付金额（元，用于显示）
     */
    @Column(name = "amount_decimal", precision = 10, scale = 2)
    private BigDecimal amountDecimal;

    /**
     * 货币类型
     */
    @Column(name = "currency", length = 3)
    private String currency = "CNY";

    /**
     * 订单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * 支付URL（二维码内容）
     */
    @Column(name = "code_url", length = 512)
    private String codeUrl;

    /**
     * 二维码Base64图片
     */
    @Column(name = "qr_code_image", columnDefinition = "TEXT")
    private String qrCodeImage;

    /**
     * 预支付交易会话标识
     */
    @Column(name = "prepay_id")
    private String prepayId;

    /**
     * 通知URL
     */
    @Column(name = "notify_url")
    private String notifyUrl;

    /**
     * 订单过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 支付完成时间
     */
    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    /**
     * 订单创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 订单更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 微信支付回调信息
     */
    @Column(name = "callback_info", columnDefinition = "TEXT")
    private String callbackInfo;

    /**
     * 错误信息
     */
    @Column(name = "error_message")
    private String errorMessage;

    /**
     * 附加数据
     */
    @Column(name = "attach")
    private String attach;

    /**
     * 是否为模拟订单
     */
    @Column(name = "is_mock")
    private Boolean isMock = true;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        /**
         * 待支付
         */
        PENDING("待支付"),
        
        /**
         * 支付中
         */
        PAYING("支付中"),
        
        /**
         * 支付成功
         */
        SUCCESS("支付成功"),
        
        /**
         * 支付失败
         */
        FAILED("支付失败"),
        
        /**
         * 已关闭
         */
        CLOSED("已关闭"),
        
        /**
         * 已过期
         */
        EXPIRED("已过期"),
        
        /**
         * 已退款
         */
        REFUNDED("已退款");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        
        // 设置默认过期时间（5分钟后）
        if (this.expireTime == null) {
            this.expireTime = LocalDateTime.now().plusMinutes(5);
        }
        
        // 将金额从元转换为分
        if (this.amountDecimal != null && this.totalAmount == null) {
            this.totalAmount = this.amountDecimal.multiply(new BigDecimal("100")).longValue();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 检查订单是否已过期
     */
    public boolean isExpired() {
        return this.expireTime != null && LocalDateTime.now().isAfter(this.expireTime);
    }

    /**
     * 检查订单是否可以支付
     */
    public boolean canPay() {
        return this.status == OrderStatus.PENDING && !isExpired();
    }

    /**
     * 检查订单是否已完成支付
     */
    public boolean isPaid() {
        return this.status == OrderStatus.SUCCESS;
    }

    /**
     * 设置支付成功
     */
    public void markAsPaid(String transactionId) {
        this.status = OrderStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paidTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 设置支付失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = OrderStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 设置订单关闭
     */
    public void markAsClosed() {
        this.status = OrderStatus.CLOSED;
        this.updatedTime = LocalDateTime.now();
    }
} 