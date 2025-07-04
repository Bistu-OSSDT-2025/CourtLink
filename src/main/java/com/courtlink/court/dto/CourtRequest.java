package com.courtlink.court.dto;

import com.courtlink.court.entity.Court;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourtRequest {
    @NotBlank(message = "场地名称不能为空")
    private String name;

    @NotNull(message = "场地类型不能为空")
    private Court.CourtType courtType;

    private String description;

    @NotNull(message = "每小时价格不能为空")
    @DecimalMin(value = "0.0", message = "价格必须大于等于0")
    private BigDecimal pricePerHour;

    private String location;
    private String facilities;
    private String rules;
} 