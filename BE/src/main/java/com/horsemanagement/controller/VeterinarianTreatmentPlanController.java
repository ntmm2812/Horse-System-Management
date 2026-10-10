package com.horsemanagement.controller;

import com.horsemanagement.config.VeterinarianPageSchemas;
import com.horsemanagement.config.VeterinarianTreatmentOpenApiExamples;
import com.horsemanagement.dto.CreatePrescriptionItemRequest;
import com.horsemanagement.dto.CreateTreatmentPlanRequest;
import com.horsemanagement.dto.PageResponse;
import com.horsemanagement.dto.PatchTreatmentPlanRequest;
import com.horsemanagement.dto.PrescriptionItemDto;
import com.horsemanagement.dto.TreatmentPlanDetailDto;
import com.horsemanagement.dto.TreatmentPlanSummaryDto;
import com.horsemanagement.entity.TreatmentPlan;
import com.horsemanagement.service.VeterinarianTreatmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.List;
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
@RequestMapping("/api/veterinarian/treatment-plans")
@Validated
@Tag(name = "Veterinarian - Treatment Plans")
public class VeterinarianTreatmentPlanController {
    private final VeterinarianTreatmentPlanService service;

    public VeterinarianTreatmentPlanController(VeterinarianTreatmentPlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List treatment plans",
        description = "Optional recordId and status filters. Zero-based pagination, size 1..100. "
            + "Sorted by startDate DESC, treatmentId DESC.")
    @ApiResponse(responseCode = "200", description = "Treatment plan page",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = VeterinarianPageSchemas.TreatmentPlanPage.class),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.TREATMENT_LIST)))
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination")
    public PageResponse<TreatmentPlanSummaryDto> list(
        @Parameter(description = "Optional positive medical record ID", example = "1")
        @RequestParam(required = false) @Positive Integer recordId,
        @Parameter(description = "Optional status: ACTIVE, COMPLETED or CANCELLED", example = "ACTIVE")
        @RequestParam(required = false) TreatmentPlan.Status status,
        @Parameter(description = "Zero-based page number (minimum 0)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size from 1 to 100", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(recordId, status, page, size);
    }

    @GetMapping("/{treatmentId}")
    @Operation(summary = "Get a treatment plan with prescription items")
    @ApiResponse(responseCode = "200", description = "Treatment details",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TreatmentPlanDetailDto.class),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.TREATMENT_DETAIL)))
    @ApiResponse(responseCode = "400", description = "treatmentId must be positive")
    @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    public TreatmentPlanDetailDto get(
        @Parameter(description = "Positive treatment plan ID", example = "1")
        @PathVariable @Positive Integer treatmentId) {
        return service.get(treatmentId);
    }

    @PostMapping
    @Operation(summary = "Create a treatment plan",
        description = "Creates an ACTIVE plan for an existing medical record. Does not change horse health or training locks. "
            + "Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Treatment plan created",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TreatmentPlanDetailDto.class),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.TREATMENT_DETAIL)))
    @ApiResponse(responseCode = "400", description = "Invalid fields or endDate before startDate")
    @ApiResponse(responseCode = "404", description = "Medical record not found")
    @ApiResponse(responseCode = "409", description = "Medical record changed during creation")
    public ResponseEntity<TreatmentPlanDetailDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreateTreatmentPlanRequest.class),
                examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.CREATE_TREATMENT)))
        @Valid @RequestBody CreateTreatmentPlanRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/veterinarian/treatment-plans/"
            + created.treatmentId())).body(created);
    }

    @PatchMapping("/{treatmentId}")
    @Operation(summary = "Update a treatment plan",
        description = "Updates only supplied non-null description, endDate and status. "
            + "Null fields retain their current values; clearing endDate is not supported. "
            + "Does not alter treatment history or horse status.")
    @ApiResponse(responseCode = "200", description = "Updated treatment details",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TreatmentPlanDetailDto.class),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.TREATMENT_DETAIL)))
    @ApiResponse(responseCode = "400", description = "Invalid status, blank description or endDate before startDate")
    @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    @ApiResponse(responseCode = "409", description = "Treatment changed during update")
    public TreatmentPlanDetailDto patch(
        @Parameter(description = "Positive treatment plan ID", example = "1")
        @PathVariable @Positive Integer treatmentId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PatchTreatmentPlanRequest.class),
                examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.PATCH_TREATMENT)))
        @Valid @RequestBody PatchTreatmentPlanRequest request) {
        return service.patch(treatmentId, request);
    }

    @GetMapping("/{treatmentId}/prescriptions")
    @Operation(summary = "List prescription items for a treatment plan",
        description = "Returns items ordered by prescriptionItemId ascending; an existing plan may have none.")
    @ApiResponse(responseCode = "200", description = "Prescription items",
        content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = PrescriptionItemDto.class)),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.PRESCRIPTION_LIST)))
    @ApiResponse(responseCode = "400", description = "treatmentId must be positive")
    @ApiResponse(responseCode = "404", description = "Treatment plan not found")
    public List<PrescriptionItemDto> listPrescriptions(
        @Parameter(description = "Positive treatment plan ID", example = "1")
        @PathVariable @Positive Integer treatmentId) {
        return service.listPrescriptions(treatmentId);
    }

    @PostMapping("/{treatmentId}/prescriptions")
    @Operation(summary = "Add a prescription item",
        description = "Supply must belong to a MEDICINE category. "
            + "This records instructions only; no inventory deduction, dispensing or dosage recommendation. "
            + "Do not submit examples to the shared database.")
    @ApiResponse(responseCode = "201", description = "Prescription item created",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PrescriptionItemDto.class),
            examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.PRESCRIPTION_CREATED)))
    @ApiResponse(responseCode = "400", description = "Invalid fields or non-medicine supply")
    @ApiResponse(responseCode = "404", description = "Treatment plan or supply not found")
    @ApiResponse(responseCode = "409", description = "Treatment or supply changed during creation")
    public ResponseEntity<PrescriptionItemDto> addPrescription(
        @Parameter(description = "Positive treatment plan ID", example = "1")
        @PathVariable @Positive Integer treatmentId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreatePrescriptionItemRequest.class),
                examples = @ExampleObject(value = VeterinarianTreatmentOpenApiExamples.CREATE_PRESCRIPTION)))
        @Valid @RequestBody CreatePrescriptionItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.addPrescription(treatmentId, request));
    }
}
