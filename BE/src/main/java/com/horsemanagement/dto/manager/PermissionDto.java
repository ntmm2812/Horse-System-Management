package com.horsemanagement.dto.manager;

/**
 * DTO quyền hạn chi tiết trong hệ thống (dbo.[PERMISSION]):
 * @param permissionId   ID quyền hạn
 * @param permissionName Tên quyền hạn (ví dụ: HORSE_READ, HORSE_WRITE, USER_MANAGE...)
 */
public record PermissionDto(int permissionId, String permissionName) {}
