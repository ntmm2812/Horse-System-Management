package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianOpenApiExamples;
import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.dto.CreateMedicalRecordRequest;
import com.horsemanagement.dto.MedicalRecordDetailDto;
import com.horsemanagement.dto.MedicalRecordSummaryDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.service.VeterinarianMedicalRecordService;
import jakarta.validation.Valid;
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
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veterinarian/medical-records")
@Validated
@Tag(name = "Veterinarian", description = "Horse health overview and medical records; authorization is not implemented yet")
public class VeterinarianMedicalRecordController {
    private final VeterinarianMedicalRecordService service;

    public VeterinarianMedicalRecordController(VeterinarianMedicalRecordService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List medical records",
        description = "Optional horseId, vetId and inclusive ISO date range filters. "
            + "page is zero-based (>= 0), size must be 1..100. "
            + "Sorted by examDate descending, then recordId descending.")
    @ApiResponse(responseCode = "200", description = "Medical record page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.MedicalRecordPage.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.MEDICAL_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid IDs, dates, date range or pagination")
    public PageResponse<MedicalRecordSummaryDto> list(
        @Parameter(description = "Optional positive horse ID", example = "3")
        @RequestParam(required = false) @Positive Integer horseId,
        @Parameter(description = "Optional positive veterinarian user ID", example = "3")
        @RequestParam(required = false) @Positive Integer vetId,
        @Parameter(description = "Inclusive start date, ISO yyyy-MM-dd", example = "2026-10-01")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @Parameter(description = "Inclusive end date, ISO yyyy-MM-dd; must be on or after fromDate",
            example = "2026-10-10")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @Parameter(description = "Zero-based page number (minimum 0)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size from 1 to 100", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(horseId, vetId, fromDate, toDate, page, size);
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "Get a medical record")
    @ApiResponse(responseCode = "200", description = "Medical record details",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MedicalRecordDetailDto.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.MEDICAL_DETAIL)))
    @ApiResponse(responseCode = "400", description = "recordId must be positive")
    @ApiResponse(responseCode = "404", description = "Medical record not found")
    public MedicalRecordDetailDto get(
        @Parameter(description = "Positive medical record ID", example = "1")
        @PathVariable @Positive Integer recordId) {
        return service.get(recordId);
    }

    @PostMapping
    @Operation(summary = "Create a medical record",
        description = "Development only: vetId comes from the request because authentication is not implemented. "
            + "Saves the record and changes only horses.health_status in one transaction. "
            + "Does not release a training lock or change readiness_status. "
            + "Do not submit this example to the shared database.")
    @ApiResponse(responseCode = "201", description = "Medical record created; Location header identifies it",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MedicalRecordDetailDto.class),
            examples = @ExampleObject(value = VeterinarianOpenApiExamples.MEDICAL_CREATED)))
    @ApiResponse(responseCode = "400", description = "Invalid body, non-veterinarian vetId, or incident belongs to another horse")
    @ApiResponse(responseCode = "404", description = "Horse or veterinarian user not found")
    @ApiResponse(responseCode = "409", description = "Incident already linked to another medical record")
    public ResponseEntity<MedicalRecordDetailDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "horseId, vetId and healthStatusAfter are required. incidentId is optional.",
            required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreateMedicalRecordRequest.class),
                examples = @ExampleObject(value = VeterinarianOpenApiExamples.MEDICAL_CREATE_REQUEST)))
        @Valid @RequestBody CreateMedicalRecordRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/veterinarian/medical-records/"
            + created.recordId())).body(created);
    }
}
