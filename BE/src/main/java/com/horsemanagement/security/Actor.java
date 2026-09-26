package com.horsemanagement.security;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;

/**
 * Đại diện cho đối tượng người dùng đã xác thực thành công trong hệ thống (Principal).
 * Được lưu trong SecurityContextHolder để lấy thông tin người dùng hiện tại ở Controller / Service.
 *
 * @param userId        ID định danh duy nhất của người dùng trong DB
 * @param fullName      Họ và tên đầy đủ
 * @param email         Địa chỉ email đăng nhập
 * @param role          Tên vai trò (Admin, Groom, Member, v.v.)
 * @param authorities   Danh sách quyền hạn và vai trò được cấp (Spring Security GrantedAuthority)
 */
public record Actor(int userId, String fullName, String email, String role, List<GrantedAuthority> authorities) {}

