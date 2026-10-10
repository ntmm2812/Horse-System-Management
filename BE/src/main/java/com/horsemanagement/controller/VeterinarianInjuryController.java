package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianInjuryOpenApiExamples;
import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.dto.CreateInjuryProgressRequest;
import com.horsemanagement.dto.CreateInjuryRequest;
import com.horsemanagement.dto.InjuryDetailDto;
import com.horsemanagement.dto.InjuryProgressDto;
import com.horsemanagement.dto.InjurySummaryDto;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchInjuryRequest;
import com.horsemanagement.entity.Injury;
import com.horsemanagement.service.VeterinarianInjuryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/veterinarian/injuries")
@Validated
@Tag(name = "Veterinarian")
public class VeterinarianInjuryController {
    private final VeterinarianInjuryService service;

    public VeterinarianInjuryController(VeterinarianInjuryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List injuries", description = "Optional horseId, status, severity and bodySystem "
        + "filters. Zero-based pagination, size 1..100. Sorted by injuryId DESC.")
    @ApiResponse(responseCode = "200", description = "Injury page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.InjuryPage.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.INJURY_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination")
    public PageResponse<InjurySummaryDto> list(
        @Parameter(description = "Optional positive horse ID", example = "3")
        @RequestParam(required = false) @Positive Integer horseId,
        @Parameter(description = "ACTIVE, RECOVERING or HEALED", example = "RECOVERING")
        @RequestParam(required = false) Injury.Status status,
        @Parameter(description = "MINOR, MODERATE or SEVERE", example = "MODERATE")
        @RequestParam(required = false) Injury.Severity severity,
        @Parameter(description = "MUSCLE, BONE, TENDON, LIGAMENT, HOOF, SKIN or OTHER",
            example = "MUSCLE")
        @RequestParam(required = false) Injury.BodySystem bodySystem,
        @Parameter(description = "Zero-based page number", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size 1..100", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(horseId, status, severity, bodySystem, page, size);
    }

    @GetMapping("/{injuryId}")
    @Operation(summary = "Get injury details")
    @ApiResponse(responseCode = "200", description = "Injury details",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = InjuryDetailDto.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.INJURY_DETAIL)))
    @ApiResponse(responseCode = "400", description = "injuryId must be positive")
    @ApiResponse(responseCode = "404", description = "Injury not found")
    public InjuryDetailDto get(@PathVariable @Positive Integer injuryId) {
        return service.get(injuryId);
    }

    @PostMapping
    @Operation(summary = "Create an injury", description = "Creates an ACTIVE injury under an existing "
        + "medical record. Does not change horse health, training locks or treatment plans. "
        + "Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Injury created",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = InjuryDetailDto.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.INJURY_DETAIL)))
    @ApiResponse(responseCode = "400", description = "Invalid fields or enum values")
    @ApiResponse(responseCode = "404", description = "Medical record not found")
    @ApiResponse(responseCode = "409", description = "Medical record changed during creation")
    public ResponseEntity<InjuryDetailDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreateInjuryRequest.class),
                examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.CREATE_INJURY)))
        @Valid @RequestBody CreateInjuryRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/veterinarian/injuries/"
            + created.injuryId())).body(created);
    }

    @PatchMapping("/{injuryId}")
    @Operation(summary = "Update injury", description = "Omitted fields remain unchanged. JSON null clears "
        + "optional fields. HEALED requires healedDate; reopening a healed injury requires explicit "
        + "healedDate: null. This never releases a training lock or changes horse health.")
    @ApiResponse(responseCode = "200", description = "Updated injury",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = InjuryDetailDto.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.INJURY_DETAIL)))
    @ApiResponse(responseCode = "400", description = "Invalid fields, status or date combination")
    @ApiResponse(responseCode = "404", description = "Injury not found")
    @ApiResponse(responseCode = "409", description = "Reopening without clearing healedDate or write conflict")
    public InjuryDetailDto patch(@PathVariable @Positive Integer injuryId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PatchInjuryRequest.class),
                examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.PATCH_INJURY)))
        @Valid @RequestBody PatchInjuryRequest request) {
        return service.patch(injuryId, request);
    }

    @GetMapping("/{injuryId}/progress")
    @Operation(summary = "List injury recovery history", description = "Sorted by logDate DESC, "
        + "then progressId DESC. Returns an empty page for an injury with no logs.")
    @ApiResponse(responseCode = "200", description = "Recovery progress page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.InjuryProgressPage.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.PROGRESS_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid injuryId or pagination")
    @ApiResponse(responseCode = "404", description = "Injury not found")
    public PageResponse<InjuryProgressDto> progress(@PathVariable @Positive Integer injuryId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.progress(injuryId, page, size);
    }

    @PostMapping("/{injuryId}/progress")
    @Operation(summary = "Add recovery progress", description = "loggedBy must have the "
        + "VETERINARIAN role. A recoveryPercent of 100 does not mark an injury HEALED or release "
        + "a training lock. Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Progress log created",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = InjuryProgressDto.class),
            examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.PROGRESS_CREATED)))
    @ApiResponse(responseCode = "400", description = "Invalid values or non-veterinarian loggedBy")
    @ApiResponse(responseCode = "404", description = "Injury or veterinarian user not found")
    @ApiResponse(responseCode = "409", description = "Injury or user changed during creation")
    public ResponseEntity<InjuryProgressDto> addProgress(@PathVariable @Positive Integer injuryId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreateInjuryProgressRequest.class),
                examples = @ExampleObject(value = VeterinarianInjuryOpenApiExamples.CREATE_PROGRESS)))
        @Valid @RequestBody CreateInjuryProgressRequest request) {
        var created = service.addProgress(injuryId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
