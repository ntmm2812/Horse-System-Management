package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.config.VeterinarianTrainingLockOpenApiExamples;
import com.horsemanagement.dto.CreateTrainingLockRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.ReleaseTrainingLockRequest;
import com.horsemanagement.dto.TrainingEligibilityDto;
import com.horsemanagement.dto.TrainingLockDto;
import com.horsemanagement.entity.TrainingLock;
import com.horsemanagement.service.VeterinarianTrainingLockService;
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
@RequestMapping("/api/veterinarian")
@Validated
@Tag(name = "Veterinarian")
public class VeterinarianTrainingLockController {
    private final VeterinarianTrainingLockService service;

    public VeterinarianTrainingLockController(VeterinarianTrainingLockService service) {
        this.service = service;
    }

    @GetMapping("/training-locks")
    @Operation(summary = "List training locks", description = "Optional horseId, status and lockLevel "
        + "filters. Sorted by lockedAt DESC, lockId DESC. Multiple ACTIVE locks per horse are permitted.")
    @ApiResponse(responseCode = "200", description = "Training lock page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.TrainingLockPage.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.LOCK_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination")
    public PageResponse<TrainingLockDto> list(
        @Parameter(description = "Optional positive horse ID", example = "3")
        @RequestParam(required = false) @Positive Integer horseId,
        @Parameter(description = "ACTIVE or RELEASED", example = "ACTIVE")
        @RequestParam(required = false) TrainingLock.Status status,
        @Parameter(description = "FULL or HEAVY_ONLY", example = "FULL")
        @RequestParam(required = false) TrainingLock.LockLevel lockLevel,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(horseId, status, lockLevel, page, size);
    }

    @GetMapping("/training-locks/{lockId}")
    @Operation(summary = "Get training lock details")
    @ApiResponse(responseCode = "200", description = "Training lock details",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TrainingLockDto.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.LOCK)))
    @ApiResponse(responseCode = "400", description = "lockId must be positive")
    @ApiResponse(responseCode = "404", description = "Training lock not found")
    public TrainingLockDto get(@PathVariable @Positive Integer lockId) {
        return service.get(lockId);
    }

    @GetMapping("/horses/{horseId}/training-locks")
    @Operation(summary = "Get one horse's training lock history",
        description = "Includes ACTIVE and RELEASED locks, newest first.")
    @ApiResponse(responseCode = "200", description = "Horse training lock history",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.TrainingLockPage.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.LOCK_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid horseId or pagination")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    public PageResponse<TrainingLockDto> history(@PathVariable @Positive Integer horseId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.horseHistory(horseId, page, size);
    }

    @PostMapping("/training-locks")
    @Operation(summary = "Create an ACTIVE training lock",
        description = "Serializes writes on the horse row and synchronizes is_training_locked in one "
            + "transaction. lockedBy is a temporary development input until authentication exists. "
            + "Does not change horse health/readiness or Head Trainer sessions. "
            + "Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Training lock created",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TrainingLockDto.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.LOCK)))
    @ApiResponse(responseCode = "400", description = "Invalid fields, non-veterinarian or injury on another horse")
    @ApiResponse(responseCode = "404", description = "Horse, veterinarian or injury not found")
    @ApiResponse(responseCode = "409", description = "Concurrent database or flag synchronization conflict")
    public ResponseEntity<TrainingLockDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreateTrainingLockRequest.class),
                examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.CREATE)))
        @Valid @RequestBody CreateTrainingLockRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/veterinarian/training-locks/"
            + created.lockId())).body(created);
    }

    @PatchMapping("/training-locks/{lockId}/release")
    @Operation(summary = "Release an ACTIVE training lock",
        description = "Recomputes is_training_locked from all remaining ACTIVE locks under a horse "
            + "row lock. A passed expectedEndAt or 100% recovery never releases a lock automatically. "
            + "Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "200", description = "Released lock",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TrainingLockDto.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.RELEASED_LOCK)))
    @ApiResponse(responseCode = "400", description = "Invalid request or non-veterinarian releasedBy")
    @ApiResponse(responseCode = "404", description = "Lock or veterinarian not found")
    @ApiResponse(responseCode = "409", description = "Already released or concurrent write conflict")
    public TrainingLockDto release(@PathVariable @Positive Integer lockId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ReleaseTrainingLockRequest.class),
                examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.RELEASE)))
        @Valid @RequestBody ReleaseTrainingLockRequest request) {
        return service.release(lockId, request);
    }

    @GetMapping("/horses/{horseId}/training-eligibility")
    @Operation(summary = "Read lock-based training restrictions",
        description = "FULL restricts LIGHT, MODERATE and HEAVY; HEAVY_ONLY restricts HEAVY. "
            + "Combines all ACTIVE locks by the strictest level. Health and readiness are returned "
            + "separately. Absence of a lock is not medical clearance and this endpoint does not "
            + "enforce Head Trainer scheduling rules.")
    @ApiResponse(responseCode = "200", description = "Current lock restrictions and horse status",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TrainingEligibilityDto.class),
            examples = @ExampleObject(value = VeterinarianTrainingLockOpenApiExamples.ELIGIBILITY)))
    @ApiResponse(responseCode = "400", description = "horseId must be positive")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    public TrainingEligibilityDto eligibility(@PathVariable @Positive Integer horseId) {
        return service.eligibility(horseId);
    }
}
