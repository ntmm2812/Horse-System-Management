package com.horsemanagement.exception;

import com.horsemanagement.controller.VeterinarianMedicalRecordController;
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
@RestControllerAdvice(assignableTypes = VeterinarianMedicalRecordController.class)
public class MedicalRecordExceptionHandler {
    @ExceptionHandler({HorseNotFoundException.class, VeterinarianNotFoundException.class,
        MedicalRecordNotFoundException.class})
    public ResponseEntity<ProblemDetail> notFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage());
    }

    @ExceptionHandler(IncidentAlreadyLinkedException.class)
    public ResponseEntity<ProblemDetail> conflict(IncidentAlreadyLinkedException exception) {
        return problem(HttpStatus.CONFLICT, "Incident already linked", exception.getMessage());
    }

    @ExceptionHandler(InvalidMedicalRecordException.class)
    public ResponseEntity<ProblemDetail> invalidMedicalRecord(InvalidMedicalRecordException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid medical record", exception.getMessage());
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
            "Check IDs, page (>= 0), size (1..100), ISO dates, and healthStatusAfter.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        var body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(title);
        return ResponseEntity.status(status).body(body);
    }
}
