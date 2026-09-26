package com.horsemanagement.dto.manager;

/**
 * DTO vai trò người dùng trong hệ thống (dbo.[ROLE]):
 * @param roleId   ID vai trò
 * @param roleName Tên vai trò (Admin, Groom, Member, Trainer, Vet...)
 */
public record RoleDto(int roleId, String roleName) {}
