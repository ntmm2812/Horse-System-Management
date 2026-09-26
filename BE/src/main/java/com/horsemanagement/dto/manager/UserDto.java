package com.horsemanagement.dto.manager;

/**
 * DTO hiển thị danh sách và thông tin tài khoản người dùng:
 * @param userId   ID người dùng
 * @param fullName Họ và tên
 * @param email    Email đăng nhập
 * @param roleId   ID vai trò
 * @param role     Tên vai trò (Admin, Groom, Member, v.v.)
 * @param status   Trạng thái tài khoản (ACTIVE, DISABLED, PENDING)
 */
public record UserDto(int userId, String fullName, String email, int roleId, String role, String status) {}
