package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianOpenApiExamples;
import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.dto.HealthMetricDto;
import com.horsemanagement.dto.HorseDetailDto;
import com.horsemanagement.dto.HorseOverviewDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.entity.Horse;
import com.horsemanagement.service.VeterinarianHorseService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veterinarian/horses")
@Validated
@Tag(name = "Veterinarian - Horse Health", description = "Horse health overview and metric history; authorization is not implemented yet")
public class VeterinarianHorseController {
    private final VeterinarianHorseService service;

    public VeterinarianHorseController(VeterinarianHorseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List horses for health overview",
        description = "Returns a page of horses with owner and manager names. healthStatus is optional; "
            + "page is zero-based (>= 0), size must be 1..100. Sorted by horseId ascending.")
    @ApiResponse(responseCode = "200", description = "Horse page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.HorseOverviewPage.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.HORSE_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid healthStatus or pagination")
    public PageResponse<HorseOverviewDto> listHorses(
        @Parameter(description = "Optional horse health status", example = "INJURED")
        @RequestParam(required = false) Horse.HealthStatus healthStatus,
        @Parameter(description = "Zero-based page number (minimum 0)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size from 1 to 100", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listHorses(healthStatus, page, size);
    }

    @GetMapping("/{horseId}")
    @Operation(summary = "Get a horse profile and current health",
        description = "Includes the latest recorded health metric when one exists.")
    @ApiResponse(responseCode = "200", description = "Horse profile",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = HorseDetailDto.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.HORSE_DETAIL)))
    @ApiResponse(responseCode = "400", description = "horseId must be positive")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    public HorseDetailDto getHorse(
        @Parameter(description = "Positive horse ID", example = "3")
        @PathVariable @Positive Integer horseId) {
        return service.getHorse(horseId);
    }

    @GetMapping("/{horseId}/health-metrics")
    @Operation(summary = "Get a horse's health metric history",
        description = "Returns an empty page for an existing horse with no metrics. "
            + "Sorted by recordedAt descending, then metricId descending.")
    @ApiResponse(responseCode = "200", description = "Health metric page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.HealthMetricPage.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.METRIC_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid horseId or pagination")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    public PageResponse<HealthMetricDto> getHealthMetrics(
        @Parameter(description = "Positive horse ID", example = "3")
        @PathVariable @Positive Integer horseId,
        @Parameter(description = "Zero-based page number (minimum 0)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size from 1 to 100", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.getHealthMetrics(horseId, page, size);
    }
}
