package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.config.VeterinarianPreventiveCareOpenApiExamples;
import com.horsemanagement.dto.CompletePreventiveCareRequest;
import com.horsemanagement.dto.CreatePreventiveCareRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchPreventiveCareRequest;
import com.horsemanagement.dto.PreventiveCareDto;
import com.horsemanagement.entity.PreventiveCareSchedule;
import com.horsemanagement.service.VeterinarianPreventiveCareService;
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
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "Veterinarian - Preventive Care")
public class VeterinarianPreventiveCareController {
    private final VeterinarianPreventiveCareService service;

    public VeterinarianPreventiveCareController(VeterinarianPreventiveCareService service) {
        this.service = service;
    }

    @GetMapping("/preventive-care")
    @Operation(summary = "List preventive care schedules", description = "Sorted by dueDate ASC, scheduleId ASC. "
        + "pendingPastDueOnly identifies PENDING rows with dueDate before today without changing stored status; "
        + "status=OVERDUE selects only rows already stored as OVERDUE.")
    @ApiResponse(responseCode = "200", description = "Schedule page", content = @Content(
        schema = @Schema(implementation = VeterinarianPageSchemas.PreventiveCarePage.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.PAGE)))
    @ApiResponse(responseCode = "400", description = "Invalid filter, date range or pagination")
    public PageResponse<PreventiveCareDto> list(
        @RequestParam(required = false) @Positive Integer horseId,
        @RequestParam(required = false) PreventiveCareSchedule.CareType careType,
        @RequestParam(required = false) PreventiveCareSchedule.Status status,
        @Parameter(description = "Inclusive due date, ISO yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDueDate,
        @Parameter(description = "Inclusive due date, ISO yyyy-MM-dd")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDueDate,
        @RequestParam(defaultValue = "false") boolean pendingPastDueOnly,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(horseId, careType, status, fromDueDate, toDueDate,
            pendingPastDueOnly, page, size);
    }

    @GetMapping("/preventive-care/{scheduleId}")
    @Operation(summary = "Get preventive care schedule details")
    @ApiResponse(responseCode = "200", description = "Schedule details", content = @Content(
        schema = @Schema(implementation = PreventiveCareDto.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.SCHEDULE)))
    @ApiResponse(responseCode = "400", description = "Invalid schedule ID")
    @ApiResponse(responseCode = "404", description = "Schedule not found")
    public PreventiveCareDto get(@PathVariable @Positive Integer scheduleId) {
        return service.get(scheduleId);
    }

    @GetMapping("/horses/{horseId}/preventive-care")
    @Operation(summary = "List one horse's preventive care history", description = "Sorted by dueDate ASC, scheduleId ASC")
    @ApiResponse(responseCode = "200", description = "Horse schedule page", content = @Content(
        schema = @Schema(implementation = VeterinarianPageSchemas.PreventiveCarePage.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.PAGE)))
    @ApiResponse(responseCode = "400", description = "Invalid status, horse ID or pagination")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    public PageResponse<PreventiveCareDto> horseHistory(@PathVariable @Positive Integer horseId,
        @RequestParam(required = false) PreventiveCareSchedule.Status status,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.horseHistory(horseId, status, page, size);
    }

    @PostMapping("/preventive-care")
    @Operation(summary = "Create PENDING preventive care schedule", description = "No recurring schedule or notification is generated. Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Created schedule", content = @Content(
        schema = @Schema(implementation = PreventiveCareDto.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.SCHEDULE)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Horse not found")
    @ApiResponse(responseCode = "409", description = "Database reference or constraint conflict")
    public ResponseEntity<PreventiveCareDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = CreatePreventiveCareRequest.class),
            examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.CREATE)))
        @Valid @RequestBody CreatePreventiveCareRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/veterinarian/preventive-care/" + result.scheduleId()))
            .body(result);
    }

    @PatchMapping("/preventive-care/{scheduleId}")
    @Operation(summary = "Update editable schedule fields", description = "Omitted fields stay unchanged. Explicit null clears description, intervalDays or notes; null for careType, dueDate or remindBeforeDays is invalid. horseId and status cannot be changed here.")
    @ApiResponse(responseCode = "200", description = "Updated schedule", content = @Content(
        schema = @Schema(implementation = PreventiveCareDto.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.SCHEDULE)))
    @ApiResponse(responseCode = "400", description = "Invalid or unsupported field")
    @ApiResponse(responseCode = "404", description = "Schedule not found")
    @ApiResponse(responseCode = "409", description = "Database conflict")
    public PreventiveCareDto patch(@PathVariable @Positive Integer scheduleId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = PatchPreventiveCareRequest.class),
            examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.PATCH)))
        @Valid @RequestBody PatchPreventiveCareRequest request) {
        return service.patch(scheduleId, request);
    }

    @PatchMapping("/preventive-care/{scheduleId}/complete")
    @Operation(summary = "Complete a PENDING or OVERDUE schedule", description = "performedBy must have VETERINARIAN role; completedDate defaults to today. Null or omitted notes preserve existing notes. Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "200", description = "Completed schedule", content = @Content(
        schema = @Schema(implementation = PreventiveCareDto.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.SCHEDULE)))
    @ApiResponse(responseCode = "400", description = "Invalid request or non-veterinarian user")
    @ApiResponse(responseCode = "404", description = "Schedule or veterinarian not found")
    @ApiResponse(responseCode = "409", description = "Already DONE or CANCELLED")
    public PreventiveCareDto complete(@PathVariable @Positive Integer scheduleId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = CompletePreventiveCareRequest.class),
            examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.COMPLETE)))
        @Valid @RequestBody CompletePreventiveCareRequest request) {
        return service.complete(scheduleId, request);
    }

    @PatchMapping("/preventive-care/{scheduleId}/cancel")
    @Operation(summary = "Cancel a PENDING or OVERDUE schedule", description = "Preserves schedule and completion history. Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "200", description = "Cancelled schedule", content = @Content(
        schema = @Schema(implementation = PreventiveCareDto.class),
        examples = @ExampleObject(value = VeterinarianPreventiveCareOpenApiExamples.SCHEDULE)))
    @ApiResponse(responseCode = "400", description = "Invalid schedule ID")
    @ApiResponse(responseCode = "404", description = "Schedule not found")
    @ApiResponse(responseCode = "409", description = "Already DONE or CANCELLED")
    public PreventiveCareDto cancel(@PathVariable @Positive Integer scheduleId) {
        return service.cancel(scheduleId);
    }
}
