<<<<<<< HEAD
package com.courtlink.booking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppointmentRequest {

    @NotNull(message = "场地ID不能为空")
    private Long courtId;

    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去的日期")
    private LocalDate appointmentDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotEmpty(message = "时间段列表不能为空")
    @Size(max = 2, message = "最多只能选择2个时间段")
    private List<Long> timeSlotIds;

    @Size(max = 200, message = "备注信息不能超过200个字符")
    private String note;

    // 验证时间段是否相邻
    @AssertTrue(message = "选择的时间段必须相邻")
    public boolean isValidTimeSlots() {
        if (timeSlotIds == null || timeSlotIds.size() <= 1) {
            return true;
        }
        
        // 对于2个时间段，需要在服务层进一步验证是否相邻
        // 这里只做基本的数量检查
        return timeSlotIds.size() <= 2;
    }

    // 验证开始时间和结束时间
    @AssertTrue(message = "结束时间必须晚于开始时间")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // 由其他验证注解处理null情况
        }
        return endTime.isAfter(startTime);
    }
=======
package com.courtlink.booking.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppointmentRequest {

    @NotNull(message = "场地ID不能为空")
    private Long courtId;

    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去的日期")
    private LocalDate appointmentDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotEmpty(message = "时间段列表不能为空")
    @Size(max = 2, message = "最多只能选择2个时间段")
    private List<Long> timeSlotIds;

    @Size(max = 200, message = "备注信息不能超过200个字符")
    private String note;

    // 验证时间段是否相邻
    @AssertTrue(message = "选择的时间段必须相邻")
    public boolean isValidTimeSlots() {
        if (timeSlotIds == null || timeSlotIds.size() <= 1) {
            return true;
        }
        
        // 对于2个时间段，需要在服务层进一步验证是否相邻
        // 这里只做基本的数量检查
        return timeSlotIds.size() <= 2;
    }

    // 验证开始时间和结束时间
    @AssertTrue(message = "结束时间必须晚于开始时间")
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // 由其他验证注解处理null情况
        }
        return endTime.isAfter(startTime);
    }
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
} 