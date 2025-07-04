package com.courtlink.booking.dto;

import com.courtlink.booking.entity.Booking;
import com.courtlink.booking.entity.BookingStatus;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long courtId;
    private LocalDateTime bookingTime;
    private Integer duration;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String remarks;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    private Long version;

    public static BookingDTO fromEntity(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .courtId(booking.getCourtId())
                .bookingTime(booking.getBookingTime())
                .duration(booking.getDuration())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .remarks(booking.getRemarks())
                .cancelReason(booking.getCancelReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .lastModifiedAt(booking.getLastModifiedAt())
                .lastModifiedBy(booking.getLastModifiedBy())
                .version(booking.getVersion())
                .build();
    }
} 