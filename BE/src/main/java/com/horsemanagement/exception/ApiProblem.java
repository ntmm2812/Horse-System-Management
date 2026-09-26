package com.horsemanagement.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Tiện ích chuẩn hóa phản hồi lỗi theo đặc tả RFC 7807 (Problem Details for HTTP APIs):
 * - Đảm bảo định dạng phản hồi lỗi thống nhất giữa Spring MVC và Spring Security.
 * - Tự động bổ sung mã lỗi đặc thù ('code': INVALID_REQUEST, NOT_FOUND, FORBIDDEN, v.v.)
 * - Bổ sung danh sách chi tiết các trường bị lỗi ('errors') để Frontend hiển thị dễ dàng.
 */
public final class ApiProblem {

    private ApiProblem() {}

    /**
     * Tạo đối tượng ProblemDetail từ mã HTTP status và thông điệp chi tiết.
     */
    public static ProblemDetail of(int status, String detail) {
        return normalize(ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), detail));
    }

    /**
     * Chuẩn hóa đối tượng ProblemDetail với mã lỗi (code) và danh sách lỗi (errors).
     */
    public static ProblemDetail normalize(ProblemDetail problem) {
        if (problem.getProperties() == null || !problem.getProperties().containsKey("code")) {
            problem.setProperty("code", switch (problem.getStatus()) {
                case 400 -> "INVALID_REQUEST";
                case 401 -> "UNAUTHENTICATED";
                case 403 -> "FORBIDDEN";
                case 404 -> "NOT_FOUND";
                case 405 -> "METHOD_NOT_ALLOWED";
                case 406 -> "NOT_ACCEPTABLE";
                case 409 -> "CONFLICT";
                case 413 -> "PAYLOAD_TOO_LARGE";
                case 415 -> "UNSUPPORTED_MEDIA_TYPE";
                default -> "INTERNAL_ERROR";
            });
        }
        if (problem.getProperties() == null || !problem.getProperties().containsKey("errors")) {
            problem.setProperty("errors", List.of());
        }
        return problem;
    }
}
