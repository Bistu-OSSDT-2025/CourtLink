package com.courtlink.dto;

import com.courtlink.enums.CourtStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "Court Request Object")
public class CourtRequest {
    @NotBlank(message = "Court name cannot be empty")
    @Size(max = 100, message = "Court name cannot exceed 100 characters")
    @Schema(description = "Court Name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Basketball Court A")
    private String name;

    @NotBlank(message = "Court location cannot be empty")
    @Size(max = 200, message = "Court location cannot exceed 200 characters")
    @Schema(description = "Court Location", requiredMode = Schema.RequiredMode.REQUIRED, example = "First Floor, Sports Center")
    private String location;

    @Size(max = 500, message = "Court description cannot exceed 500 characters")
    @Schema(description = "Court Description", example = "Standard basketball court with 4 hoops")
    private String description;

    @NotNull(message = "Court status cannot be empty")
    @Schema(description = "Court Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "AVAILABLE")
    private CourtStatus status;
} 