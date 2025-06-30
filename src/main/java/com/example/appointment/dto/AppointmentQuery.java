package com.example.appointment.dto;

import com.example.appointment.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 预约查询条件对象
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "预约查询条件")
public class AppointmentQuery {

    @Schema(description = "用户ID", example = "user123")
    private String userId;

    @Schema(description = "服务提供者ID", example = "provider456")
    private String providerId;

    @Schema(description = "服务类型", example = "篮球场")
    private String serviceType;

    @Schema(description = "预约状态", example = "PENDING")
    private Appointment.AppointmentStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间范围-起始", example = "2024-03-25T00:00:00")
    private LocalDateTime startTimeFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间范围-结束", example = "2024-03-25T23:59:59")
    private LocalDateTime startTimeTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间范围-起始", example = "2024-03-25T00:00:00")
    private LocalDateTime endTimeFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间范围-结束", example = "2024-03-25T23:59:59")
    private LocalDateTime endTimeTo;

    @Schema(description = "页码", example = "0")
    private Integer page = 0;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "排序字段", example = "startTime")
    private String sortBy = "startTime";

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
} 