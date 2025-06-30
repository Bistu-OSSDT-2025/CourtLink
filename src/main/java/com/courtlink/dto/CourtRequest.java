package com.courtlink.dto;

import com.courtlink.enums.CourtStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Court Request Object")
public class CourtRequest {
    @NotBlank(message = "Court name cannot be empty")
    @Size(max = 100, message = "Court name cannot exceed 100 characters")
    @ApiModelProperty(value = "Court Name", required = true, example = "Basketball Court A")
    private String name;

    @NotBlank(message = "Court location cannot be empty")
    @Size(max = 200, message = "Court location cannot exceed 200 characters")
    @ApiModelProperty(value = "Court Location", required = true, example = "First Floor, Sports Center")
    private String location;

    @Size(max = 500, message = "Court description cannot exceed 500 characters")
    @ApiModelProperty(value = "Court Description", example = "Standard basketball court with 4 hoops")
    private String description;

    @NotNull(message = "Court status cannot be empty")
    @ApiModelProperty(value = "Court Status", required = true, example = "AVAILABLE")
    private CourtStatus status;
} 