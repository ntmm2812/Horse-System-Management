package com.horsemanagement.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Bộ xử lý ngoại lệ toàn cục cho các REST Controller (@RestControllerAdvice):
 * - Bắt và chuyển đổi mọi ngoại lệ thành định dạng chuẩn RFC 7807 (ProblemDetail).
 * - Xử lý lỗi validation dữ liệu (@Valid, @NotNull, @NotBlank, @Size, v.v.).
 * - Xử lý lỗi quyền truy cập (AccessDeniedException -> 403 Forbidden).
 * - Xử lý lỗi trùng lặp khóa chính hoặc vi phạm ràng buộc dữ liệu SQL -> 409 Conflict.
 */
@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {

    /**
     * Bắt ngoại lệ nghiệp vụ tùy chỉnh ApiException do lập trình viên chủ động ném.
     */
    @ExceptionHandler(ApiException.class)
    ProblemDetail api(ApiException ex) {
        return ApiProblem.of(ex.status(), ex.getMessage());
    }

    /**
     * Bắt lỗi không đủ thẩm quyền truy cập (Spring Security 403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail denied() {
        return ApiProblem.of(403, "Bạn không có quyền thực hiện thao tác này.");
    }

    /**
     * Bắt lỗi vi phạm tính toàn vẹn dữ liệu (trùng email, khóa ngoại, phiên bản lạc quan Optimistic Locking).
     */
    @ExceptionHandler({DataIntegrityViolationException.class, ConcurrencyFailureException.class})
    ProblemDetail conflict() {
        return ApiProblem.of(409, "Dữ liệu trùng, đang được sử dụng hoặc vừa thay đổi. Hãy tải lại và kiểm tra.");
    }

    /**
     * Bắt lỗi vi phạm ràng buộc tham số (@Min, @Max, v.v.)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalid() {
        return ApiProblem.of(400, "Tham số không hợp lệ.");
    }

    /**
     * Bắt lỗi dữ liệu body không hợp lệ (@Valid DTO) và trả về chi tiết từng trường bị lỗi.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ApiProblem.of(400, "Dữ liệu nhập không hợp lệ.");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(e -> java.util.Map.of("field", e.getField(), "message", e.getDefaultMessage())).toList());
        return createResponseEntity(problem, headers, status, request);
    }

    /**
     * Tự động chuẩn hóa ProblemDetail trước khi phản hồi về Client.
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (body instanceof ProblemDetail problem) {
            ApiProblem.normalize(problem);
        }
        return super.createResponseEntity(body, headers, status, request);
    }
}
