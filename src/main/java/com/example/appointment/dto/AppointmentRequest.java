package com.example.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约创建请求对象
 * 
 * @author Your Name
 * @version 1.0.0
 */
@Data
@Schema(description = "预约创建请求")
public class AppointmentRequest {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "user123")
    private String userId;

    @NotBlank(message = "服务提供者ID不能为空")
    @Schema(description = "服务提供者ID", required = true, example = "provider456")
    private String providerId;

    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型", required = true, example = "篮球场")
    private String serviceType;

    @NotNull(message = "预约开始时间不能为空")
    @Future(message = "预约开始时间必须是未来时间")
    @Schema(description = "预约开始时间", required = true, example = "2024-03-25T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    @Future(message = "预约结束时间必须是未来时间")
    @Schema(description = "预约结束时间", required = true, example = "2024-03-25T12:00:00")
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "金额不能为负数")
    @Schema(description = "预约金额", example = "100.00")
    private BigDecimal amount;

    @Size(max = 500, message = "备注信息不能超过500字符")
    @Schema(description = "备注信息", example = "请准备篮球")
    private String notes;
} 