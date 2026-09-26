package com.horsemanagement.controller;

import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.security.Actor;
import com.horsemanagement.service.ManagerService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static com.horsemanagement.dto.manager.Requests.*;

/**
 * Controller dành riêng cho Club Manager (Ánh xạ vào Role 'Admin' trong hệ thống).
 * Cung cấp toàn bộ các chức năng quản trị:
 * - Quản lý hồ sơ ngựa (CRUD, lưu trữ hồ sơ)
 * - Quản lý nhân sự (CRUD, khóa/mở tài khoản, duyệt tài khoản, đặt lại mật khẩu)
 * - Quản lý phân quyền RBAC (Role & Permissions)
 * - Quản lý kho vật tư (CRUD, nhập/xuất kho)
 * - Xem nhật ký kiểm toán (Audit Logs)
 */
@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    private final ManagerService service;

    public ManagerController(ManagerService service) { 
        this.service = service; 
    }

    // ==========================================
    // 1. QUẢN LÝ HỒ SƠ NGỰA (HORSE CRUD)
    // ==========================================

    /**
     * Lấy danh sách ngựa (chưa bị lưu trữ), hỗ trợ tìm kiếm theo tên và phân trang.
     * Yêu cầu quyền: MANAGE_HORSES
     */
    @GetMapping("/horses") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public PageDto<HorseDto> horses(
            @RequestParam(defaultValue="") String search,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.horses(search, page, size); 
    }

    /**
     * Xem thông tin chi tiết một con ngựa theo ID.
     */
    @GetMapping("/horses/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public HorseDto horse(@PathVariable int id) { 
        return service.horse(id); 
    }

    /**
     * Thêm mới một hồ sơ ngựa vào hệ thống (yêu cầu gán chủ sở hữu hợp lệ có role Owner).
     */
    @PostMapping("/horses") 
    @ResponseStatus(HttpStatus.CREATED) 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public HorseDto createHorse(@AuthenticationPrincipal Actor actor, @Valid @RequestBody HorseInput input) { 
        return service.createHorse(actor, input); 
    }

    /**
     * Cập nhật thông tin hồ sơ ngựa theo ID.
     */
    @PutMapping("/horses/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public HorseDto updateHorse(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody HorseInput input) { 
        return service.updateHorse(actor, id, input); 
    }

    /**
     * Lưu trữ (Soft Delete) hồ sơ ngựa, không xóa cứng khỏi CSDL để bảo tồn lịch sử.
     */
    @DeleteMapping("/horses/{id}") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    @PreAuthorize("hasAuthority('MANAGE_HORSES')")
    public void archiveHorse(@AuthenticationPrincipal Actor actor, @PathVariable int id) { 
        service.archiveHorse(actor, id); 
    }

    // ==========================================
    // 2. QUẢN LÝ NHÂN SỰ & TÀI KHOẢN (USER CRUD)
    // ==========================================

    /**
     * Lấy danh sách người dùng/nhân sự, có bộ lọc theo từ khóa, trạng thái (status) và vai trò (role).
     * Yêu cầu quyền: MANAGE_USERS
     */
    @GetMapping("/users") 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public PageDto<UserDto> users(
            @RequestParam(defaultValue="") String search,
            @RequestParam(defaultValue="") String status,
            @RequestParam(defaultValue="") String role,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.users(search, status, role, page, size); 
    }

    /**
     * Xem thông tin chi tiết của một tài khoản theo UserID.
     */
    @GetMapping("/users/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserDto user(@PathVariable int id) { 
        return service.user(id); 
    }

    /**
     * Tạo tài khoản nhân sự mới (Admin, Groom, Trainer, Vet...). Tự động băm mật khẩu.
     */
    @PostMapping("/users") 
    @ResponseStatus(HttpStatus.CREATED) 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserDto createUser(@AuthenticationPrincipal Actor actor, @Valid @RequestBody UserInput input) { 
        return service.createUser(actor, input); 
    }

    /**
     * Cập nhật thông tin tài khoản (Họ tên, Email, RoleID). 
     * Ngăn chặn việc tự gỡ quyền Admin của chính mình.
     */
    @PutMapping("/users/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserDto updateUser(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody UserUpdate input) { 
        return service.updateUser(actor, id, input); 
    }

    /**
     * Khóa (DISABLED) hoặc mở khóa (ACTIVE) tài khoản. 
     * Bảo vệ không cho phép khóa tài khoản Admin cuối cùng hoặc tài khoản đang đăng nhập.
     */
    @PatchMapping("/users/{id}/status") 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserDto status(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody StatusInput input) { 
        return service.status(actor, id, input.status()); 
    }

    /**
     * Phê duyệt (approved: true) hoặc từ chối (approved: false) tài khoản PENDING.
     * Yêu cầu quyền: APPROVE_ACCOUNTS
     */
    @PostMapping("/users/{id}/approval") 
    @PreAuthorize("hasAuthority('APPROVE_ACCOUNTS')")
    public UserDto approve(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody Approval input) { 
        return service.approve(actor, id, input.approved()); 
    }

    /**
     * Đặt lại mật khẩu cho nhân sự (buộc thu hồi toàn bộ token đăng nhập cũ).
     */
    @PutMapping("/users/{id}/password") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public void password(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody ResetPassword input) { 
        service.resetPassword(actor, id, input.password()); 
    }

    // ==========================================
    // 3. PHÂN QUYỀN RBAC (ROLES & PERMISSIONS)
    // ==========================================

    /**
     * Lấy danh sách toàn bộ các vai trò (Roles) trong hệ thống.
     */
    @GetMapping("/roles") 
    @PreAuthorize("hasAuthority('MANAGE_USERS') or hasAuthority('MANAGE_RBAC')")
    public List<RoleDto> roles() { 
        return service.roles(); 
    }

    /**
     * Lấy danh sách toàn bộ quyền hạn (Permissions) có trong hệ thống.
     */
    @GetMapping("/permissions") 
    @PreAuthorize("hasAuthority('MANAGE_RBAC')")
    public List<PermissionDto> permissions() { 
        return service.permissions(); 
    }

    /**
     * Xem danh sách các quyền hạn được gán cho một Role cụ thể.
     */
    @GetMapping("/roles/{id}/permissions") 
    @PreAuthorize("hasAuthority('MANAGE_RBAC')")
    public List<PermissionDto> rolePermissions(@PathVariable int id) { 
        return service.rolePermissions(id); 
    }

    /**
     * Cập nhật danh sách quyền hạn cho một Role (Bắt buộc Role Admin phải giữ các quyền quản trị cốt lõi).
     */
    @PutMapping("/roles/{id}/permissions") 
    @PreAuthorize("hasAuthority('MANAGE_RBAC')")
    public List<PermissionDto> permissions(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody PermissionInput input) { 
        return service.setPermissions(actor, id, input.permissionIds()); 
    }

    // ==========================================
    // 4. QUẢN LÝ KHO VẬT TƯ (SUPPLY CRUD)
    // ==========================================

    /**
     * Lấy danh mục vật tư trong kho (chưa bị lưu trữ), hỗ trợ tìm kiếm và phân trang.
     */
    @GetMapping("/supplies") 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public PageDto<SupplyDto> supplies(
            @RequestParam(defaultValue="") String search,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.supplies(search, page, size); 
    }

    /**
     * Xem thông tin chi tiết một mặt hàng vật tư.
     */
    @GetMapping("/supplies/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public SupplyDto supply(@PathVariable int id) { 
        return service.supply(id); 
    }

    /**
     * Thêm mặt hàng vật tư mới vào kho.
     */
    @PostMapping("/supplies") 
    @ResponseStatus(HttpStatus.CREATED) 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public SupplyDto createSupply(@AuthenticationPrincipal Actor actor, @Valid @RequestBody SupplyInput input) { 
        return service.createSupply(actor, input); 
    }

    /**
     * Cập nhật thông tin vật tư (Tên, phân loại, người quản lý). Sử dụng version để chống ghi đè đồng thời.
     */
    @PutMapping("/supplies/{id}") 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public SupplyDto updateSupply(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody SupplyUpdate input) { 
        return service.updateSupply(actor, id, input); 
    }

    /**
     * Điều chỉnh số lượng kho (Nhập kho + / Xuất kho -) có ghi rõ lý do. Kiểm tra không để âm kho.
     */
    @PostMapping("/supplies/{id}/adjustments") 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public SupplyDto stock(@AuthenticationPrincipal Actor actor, @PathVariable int id, @Valid @RequestBody StockAdjustment input) { 
        return service.stock(actor, id, input); 
    }

    /**
     * Lưu trữ mặt hàng vật tư khi không còn sử dụng.
     */
    @DeleteMapping("/supplies/{id}") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    @PreAuthorize("hasAuthority('MANAGE_SUPPLIES')")
    public void archiveSupply(@AuthenticationPrincipal Actor actor, @PathVariable int id, @RequestParam int version) { 
        service.archiveSupply(actor, id, version); 
    }

    // ==========================================
    // 5. NHẬT KÝ KIỂM TOÁN (AUDIT LOGS)
    // ==========================================

    /**
     * Xem danh sách nhật ký thao tác (Ai làm gì, trên bảng nào, lúc mấy giờ).
     * Yêu cầu quyền: VIEW_AUDIT_LOG
     */
    @GetMapping("/audit-logs") 
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOG')")
    public PageDto<AuditLogDto> audit(
            @RequestParam(required=false) Integer userId,
            @RequestParam(defaultValue="") String table,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) { 
        return service.audit(userId, table, page, size); 
    }
}
