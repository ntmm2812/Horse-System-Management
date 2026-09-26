package com.horsemanagement.controller;

import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.auth.AuthRequests.*;
import com.horsemanagement.security.Actor;
import com.horsemanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý xác thực và quản lý phiên đăng nhập:
 * - Đăng nhập, đăng ký tài khoản Owner chờ duyệt
 * - Lấy thông tin tài khoản hiện tại (/me)
 * - Đăng xuất và đổi mật khẩu
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { 
        this.service = service; 
    }

    /**
     * API Đăng nhập hệ thống bằng email và mật khẩu.
     * Trả về Bearer token và thông tin người dùng kèm role, permissions.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody Login input) { 
        return service.login(input); 
    }

    /**
     * API Đăng ký tài khoản dành cho chủ ngựa (Owner).
     * Tài khoản sau khi đăng ký sẽ ở trạng thái PENDING để chờ Club Manager phê duyệt.
     */
    @PostMapping("/register") 
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationDto register(@Valid @RequestBody Register input) { 
        return service.register(input); 
    }

    /**
     * API Lấy thông tin cá nhân của người dùng đang đăng nhập.
     * Sử dụng token Bearer trong header để định danh Actor.
     */
    @GetMapping("/me")
    public AuthUser me(@AuthenticationPrincipal Actor actor) { 
        return service.me(actor); 
    }

    /**
     * API Đăng xuất: Thu hồi token hiện tại trong database ngay lập tức.
     */
    @PostMapping("/logout") 
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String header) { 
        service.logout(header.substring(7)); 
    }

    /**
     * API Đổi mật khẩu: Yêu cầu mật khẩu hiện tại chính xác, sau đó mã hóa mật khẩu mới
     * và thu hồi toàn bộ token cũ của người dùng để bắt buộc đăng nhập lại.
     */
    @PutMapping("/password") 
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@AuthenticationPrincipal Actor actor, @Valid @RequestBody PasswordChange input) { 
        service.password(actor, input); 
    }
}
