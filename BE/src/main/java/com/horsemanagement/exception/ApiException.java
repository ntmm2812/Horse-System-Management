package com.horsemanagement.exception;

/**
 * Ngoại lệ nghiệp vụ dùng chung của ứng dụng (Application Business Exception):
 * - Đóng gói mã trạng thái HTTP (status code) và thông báo lỗi tương ứng.
 * - Giúp ném lỗi ở Service hoặc DAO và tự động được bắt bởi ApiErrors để trả về RFC 7807 cho Client.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Mã trạng thái HTTP (400, 401, 403, 404, 409, 413, v.v.)
     */
    public int status() {
        return status;
    }

    /**
     * Tạo nhanh ngoại lệ 404 Not Found (Không tìm thấy dữ liệu).
     */
    public static ApiException notFound() {
        return new ApiException(404, "Không tìm thấy dữ liệu.");
    }

    /**
     * Tạo nhanh ngoại lệ 409 Conflict (Xung đột dữ liệu / Đã tồn tại).
     */
    public static ApiException conflict(String message) {
        return new ApiException(409, message);
    }
}
