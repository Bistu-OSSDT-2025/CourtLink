package com.courtlink.dto;

import com.courtlink.enums.CourtStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "Court Response Object")
public class CourtResponse {
    @ApiModelProperty(value = "Court ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "Court Name", example = "Basketball Court A")
    private String name;

    @ApiModelProperty(value = "Court Location", example = "First Floor, Sports Center")
    private String location;

    @ApiModelProperty(value = "Court Description", example = "Standard basketball court with 4 hoops")
    private String description;

    @ApiModelProperty(value = "Court Status", example = "AVAILABLE")
    private CourtStatus status;

    @ApiModelProperty(value = "Creation Time")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "Update Time")
    private LocalDateTime updatedAt;
} 