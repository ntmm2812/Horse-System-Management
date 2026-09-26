package com.horsemanagement.dao.manager;

import com.horsemanagement.dao.support.Database;
import com.horsemanagement.dto.manager.RoleDto;
import com.horsemanagement.dto.manager.PermissionDto;
import java.util.*;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) quản lý vai trò và phân quyền (RBAC - Role Based Access Control):
 * - Quản lý danh mục các vai trò trong hệ thống (dbo.[ROLE]: Admin, Groom, Member, v.v.).
 * - Quản lý danh mục các quyền hạn chi tiết (dbo.[PERMISSION]).
 * - Gán và thay thế danh sách quyền hạn cho từng vai trò (dbo.ROLEPERMISSION).
 * - Truy vấn danh sách quyền thực tế của một người dùng theo UserID để nạp vào Spring Security Context.
 */
@Repository
public class RoleDao {

    private final Database db;

    public RoleDao(Database db) {
        this.db = db;
    }

    /**
     * Lấy toàn bộ danh sách vai trò hiện có trong hệ thống.
     */
    public List<RoleDto> findAll() {
        return db.query("SELECT RoleID AS roleId, RoleName AS roleName FROM dbo.[ROLE] ORDER BY RoleID", RoleDto.class);
    }

    /**
     * Tìm thông tin vai trò theo RoleID.
     */
    public RoleDto findById(int id) {
        return db.queryOne("SELECT RoleID AS roleId, RoleName AS roleName FROM dbo.[ROLE] WHERE RoleID=?", RoleDto.class, id);
    }

    /**
     * Tìm thông tin vai trò theo tên vai trò (Admin, Groom, v.v.).
     */
    public RoleDto findByName(String name) {
        return db.queryOne("SELECT RoleID AS roleId, RoleName AS roleName FROM dbo.[ROLE] WHERE RoleName=?", RoleDto.class, name);
    }

    /**
     * Lấy tất cả các quyền hạn (Permissions) có trong hệ thống.
     */
    public List<PermissionDto> permissions() {
        return db.query("SELECT PermissionID AS permissionId, PermissionName AS permissionName FROM dbo.[PERMISSION] ORDER BY PermissionID", PermissionDto.class);
    }

    /**
     * Lấy thông tin một quyền hạn theo PermissionID.
     */
    public PermissionDto permission(int id) {
        return db.queryOne("SELECT PermissionID AS permissionId, PermissionName AS permissionName FROM dbo.[PERMISSION] WHERE PermissionID=?", PermissionDto.class, id);
    }

    /**
     * Lấy danh sách các quyền hạn đã được gán cho một vai trò cụ thể.
     */
    public List<PermissionDto> permissionsForRole(int id) {
        return db.query("""
            SELECT p.PermissionID AS permissionId, p.PermissionName AS permissionName
            FROM dbo.ROLEPERMISSION rp
            JOIN dbo.[PERMISSION] p ON p.PermissionID = rp.PermissionID
            WHERE rp.RoleID = ? ORDER BY p.PermissionID
            """, PermissionDto.class, id);
    }

    /**
     * Thay thế toàn bộ quyền hạn của một vai trò bằng danh sách ID mới.
     */
    public void replacePermissions(int roleId, Set<Integer> ids) {
        db.update("DELETE FROM dbo.ROLEPERMISSION WHERE RoleID=?", roleId);
        for (int id : ids) {
            db.update("INSERT INTO dbo.ROLEPERMISSION (RoleID, PermissionID) VALUES (?,?)", roleId, id);
        }
    }

    /**
     * Truy vấn toàn bộ tên quyền (PermissionName) của một người dùng theo UserID để cấp quyền trong Spring Security.
     */
    public List<String> permissionsForUser(int userId) {
        return db.query("""
            SELECT p.PermissionID AS permissionId, p.PermissionName AS permissionName
            FROM dbo.[USER] u
            JOIN dbo.ROLEPERMISSION rp ON rp.RoleID = u.RoleID
            JOIN dbo.[PERMISSION] p ON p.PermissionID = rp.PermissionID
            WHERE u.UserID = ?
            """, PermissionDto.class, userId)
            .stream().map(PermissionDto::permissionName).toList();
    }
}
