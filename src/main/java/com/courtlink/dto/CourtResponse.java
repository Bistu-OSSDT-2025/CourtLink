package com.courtlink.dto;

import com.courtlink.enums.CourtStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Court Response Object")
public class CourtResponse {
    @Schema(description = "Court ID", example = "1")
    private Long id;

    @Schema(description = "Court Name", example = "Basketball Court A")
    private String name;

    @Schema(description = "Court Location", example = "First Floor, Sports Center")
    private String location;

    @Schema(description = "Court Description", example = "Standard basketball court with 4 hoops")
    private String description;

    @Schema(description = "Court Status", example = "AVAILABLE")
    private CourtStatus status;

    @Schema(description = "Creation Time")
    private LocalDateTime createdAt;

    @Schema(description = "Update Time")
    private LocalDateTime updatedAt;
} 