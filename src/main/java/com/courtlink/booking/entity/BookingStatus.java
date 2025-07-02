package com.courtlink.booking.entity;

public enum BookingStatus {
    PENDING("待支付"),
    PAID("已支付"),
    CONFIRMED("已确认"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REFUNDED("已退款");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 