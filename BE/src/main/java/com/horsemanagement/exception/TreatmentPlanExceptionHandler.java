package com.horsemanagement.exception;

import com.horsemanagement.controller.VeterinarianTreatmentPlanController;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = VeterinarianTreatmentPlanController.class)
public class TreatmentPlanExceptionHandler {
    @ExceptionHandler({TreatmentPlanNotFoundException.class, MedicalRecordNotFoundException.class,
        SupplyNotFoundException.class})
    public ResponseEntity<ProblemDetail> notFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage());
    }

    @ExceptionHandler(InvalidTreatmentPlanException.class)
    public ResponseEntity<ProblemDetail> invalid(InvalidTreatmentPlanException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid treatment request", exception.getMessage());
    }

    @ExceptionHandler(TreatmentConflictException.class)
    public ResponseEntity<ProblemDetail> conflict(TreatmentConflictException exception) {
        return problem(HttpStatus.CONFLICT, "Treatment conflict", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalidBody(MethodArgumentNotValidException exception) {
        var details = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .distinct().collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", details);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
        MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> invalidParameters(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
            "Check IDs, status, route, page (>= 0), size (1..100), dates and request fields.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        var body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return ResponseEntity.status(status).body(body);
    }
}
