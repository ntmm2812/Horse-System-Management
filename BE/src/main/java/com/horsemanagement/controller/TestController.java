package com.horsemanagement.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller kiểm tra trạng thái hoạt động (Healthcheck) của Backend.
 * Endpoint này công khai (permitAll), không yêu cầu đăng nhập.
 */
@RestController
public class TestController {

    /**
     * Endpoint kiểm tra kết nối nhanh từ trình duyệt hoặc kiểm thử tự động.
     * URL: GET /api/test
     */
    @GetMapping(value = "/api/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test() {
        return "Horse Management Backend is running";
    }
}
