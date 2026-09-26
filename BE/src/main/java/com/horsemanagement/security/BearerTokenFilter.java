package com.horsemanagement.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Bộ lọc HTTP (HTTP Filter) chặn mọi request để kiểm tra Bearer Token:
 * - Trích xuất token từ Header 'Authorization: Bearer <token>'
 * - Xác thực token với DB thông qua TokenService
 * - Nếu token hợp lệ, nạp Actor và quyền vào SecurityContextHolder
 * - Kế thừa OncePerRequestFilter đảm bảo chỉ chạy 1 lần cho mỗi HTTP request
 */
public class BearerTokenFilter extends OncePerRequestFilter {

    private final TokenService tokens;

    public BearerTokenFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokens.authenticate(token).ifPresent(actor ->
                SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(actor, null, actor.authorities())));
        }
        chain.doFilter(request, response);
    }
}

