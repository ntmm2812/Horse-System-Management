package com.horsemanagement.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class VeterinarianExceptionHandler {
    @ExceptionHandler(HorseNotFoundException.class)
    public ResponseEntity<ProblemDetail> horseNotFound(HorseNotFoundException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Horse not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
        MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> invalidRequest(Exception exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
            "Invalid request parameter. page must be >= 0, size must be 1..100, "
                + "horseId must be positive, and healthStatus must be one of "
                + "ELIGIBLE, MONITORING, INJURED, QUARANTINE.");
        problem.setTitle("Invalid request");
        return ResponseEntity.badRequest().body(problem);
    }
}
