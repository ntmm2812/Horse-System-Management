package com.horsemanagement.service;

import com.horsemanagement.dao.auth.*;
import com.horsemanagement.dao.manager.*;
import com.horsemanagement.dao.groom.*;
import com.horsemanagement.dto.auth.*;
import com.horsemanagement.dto.common.*;
import com.horsemanagement.dto.manager.*;
import com.horsemanagement.dto.groom.*;
import com.horsemanagement.dto.manager.Requests.*;
import com.horsemanagement.exception.ApiException;
import com.horsemanagement.security.Actor;
import com.horsemanagement.security.TokenService;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import static com.horsemanagement.service.Pagination.offset;

/**
 * Service xử lý toàn bộ logic nghiệp vụ của Club Manager:
 * - Quản lý hồ sơ ngựa (CRUD, kiểm tra chủ sở hữu, khởi tạo sức khỏe, lưu trữ)
 * - Quản lý nhân sự (CRUD, đổi trạng thái, duyệt đăng ký, băm mật khẩu)
 * - Quản lý phân quyền RBAC (Role và Permission)
 * - Quản lý kho vật tư (CRUD, xuất nhập kho có cơ chế khóa lạc quan)
 * - Truy vấn nhật ký kiểm toán (Audit Logs)
 */
@Service
public class ManagerService {
    private final HorseDao horses;
    private final UserDao users;
    private final RoleDao roles;
    private final SupplyDao supplies;
    private final AuditDao logs;
    private final AuditService audit;
    private final PasswordEncoder passwords;
    private final TokenService tokens;

    public ManagerService(HorseDao horses, UserDao users, RoleDao roles, SupplyDao supplies, AuditDao logs,
                          AuditService audit, PasswordEncoder passwords, TokenService tokens) {
        this.horses = horses;
        this.users = users;
        this.roles = roles;
        this.supplies = supplies;
        this.logs = logs;
        this.audit = audit;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    // ==========================================
    // 1. NGHIỆP VỤ HỒ SƠ NGỰA (HORSE)
    // ==========================================

    /**
     * Lấy danh sách ngựa đang hoạt động (Archived = 0), tìm kiếm theo tên và phân trang.
     */
    public PageDto<HorseDto> horses(String search, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(horses.findActive(search, skip, size), horses.countActive(search), page, size);
    }

    /**
     * Tìm thông tin chi tiết một con ngựa theo ID.
     */
    public HorseDto horse(int id) { 
        return horses.findActiveById(id); 
    }

    /**
     * Tạo mới hồ sơ ngựa:
     * - Kiểm tra OwnerID phải là người dùng có vai trò 'Owner' và đang ACTIVE.
     * - Khởi tạo bản ghi tình trạng sức khỏe ban đầu ('Unknown').
     * - Ghi lại nhật ký kiểm toán (CREATE).
     */
    @Transactional
    public HorseDto createHorse(Actor actor, HorseInput input) {
        requireRole(input.ownerId(), "Owner");
        int id = horses.insert(input);
        horses.initializeHealth(id);
        audit.record(actor.userId(), "CREATE", "Horse", id);
        return horse(id);
    }

    /**
     * Cập nhật thông tin hồ sơ ngựa và ghi nhật ký kiểm toán (UPDATE).
     */
    @Transactional
    public HorseDto updateHorse(Actor actor, int id, HorseInput input) {
        requireRole(input.ownerId(), "Owner");
        exists(horses.update(id, input));
        audit.record(actor.userId(), "UPDATE", "Horse", id);
        return horse(id);
    }

    /**
     * Lưu trữ hồ sơ ngựa (Soft Delete) và ghi nhật ký kiểm toán (ARCHIVE).
     */
    @Transactional
    public void archiveHorse(Actor actor, int id) {
        exists(horses.archive(id));
        audit.record(actor.userId(), "ARCHIVE", "Horse", id);
    }

    // ==========================================
    // 2. NGHIỆP VỤ NHÂN SỰ & TÀI KHOẢN (USER)
    // ==========================================

    /**
     * Tìm kiếm và phân trang danh sách người dùng với bộ lọc trạng thái và vai trò.
     */
    public PageDto<UserDto> users(String search, String status, String role, int page, int size) {
        int skip = offset(page, size);
        if (!status.isEmpty() && !Set.of("PENDING", "ACTIVE", "DISABLED", "REJECTED").contains(status)) {
            throw new ApiException(400, "Trạng thái tài khoản không hợp lệ.");
        }
        if (!role.isEmpty() && !Set.of("Admin", "Owner", "Trainer", "Vet", "Groom").contains(role)) {
            throw new ApiException(400, "Vai trò phải là Admin, Owner, Trainer, Vet hoặc Groom.");
        }
        return PageDto.of(users.search(search, status, role, skip, size), users.count(search, status, role), page, size);
    }

    /**
     * Tìm người dùng theo UserID.
     */
    public UserDto user(int id) { 
        return users.findById(id); 
    }

    /**
     * Tạo tài khoản nhân sự mới: Tự động mã hóa mật khẩu bằng BCrypt và đặt trạng thái ACTIVE.
     */
    @Transactional
    public UserDto createUser(Actor actor, UserInput input) {
        roles.findById(input.roleId());
        int id = users.insert(input.fullName().trim(), input.email().trim().toLowerCase(Locale.ROOT), encode(input.password()), input.roleId(), "ACTIVE");
        audit.record(actor.userId(), "CREATE", "USER", id);
        return user(id);
    }

    /**
     * Cập nhật thông tin nhân sự:
     * - Ngăn chặn người dùng tự gỡ vai trò Admin của chính mình.
     * - Bảo vệ không làm mất Admin hoạt động cuối cùng của hệ thống.
     * - Thu hồi token để bắt buộc cập nhật lại thông tin ở phiên sau.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto updateUser(Actor actor, int id, UserUpdate input) {
        var before = user(id);
        String role = roles.findById(input.roleId()).roleName();
        if (id == actor.userId() && !"Admin".equals(role)) {
            throw ApiException.conflict("Không thể tự gỡ vai trò Admin.");
        }
        if ("Admin".equals(before.role()) && !"Admin".equals(role)) {
            protectLastAdmin(before);
        }
        users.update(id, input);
        tokens.revokeUser(id);
        audit.record(actor.userId(), "UPDATE", "USER", id);
        return user(id);
    }

    /**
     * Đổi trạng thái tài khoản (ACTIVE / DISABLED):
     * - Không cho phép tự khóa tài khoản của chính mình.
     * - Ngăn chặn khóa tài khoản Admin cuối cùng còn hoạt động.
     * - Thu hồi toàn bộ token đăng nhập của tài khoản đó.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDto status(Actor actor, int id, AccountState state) {
        var before = user(id);
        if (id == actor.userId()) {
            throw ApiException.conflict("Không thể tự khóa tài khoản đang đăng nhập.");
        }
        if (!List.of("ACTIVE", "DISABLED").contains(before.status())) {
            throw ApiException.conflict("Tài khoản đăng ký cần xử lý qua chức năng duyệt.");
        }
        if (state == AccountState.DISABLED) {
            protectLastAdmin(before);
        }
        users.setStatus(id, state.name());
        tokens.revokeUser(id);
        audit.record(actor.userId(), "SET_" + state, "USER", id);
        return user(id);
    }

    /**
     * Phê duyệt (approved: true -> ACTIVE) hoặc từ chối (approved: false -> REJECTED) tài khoản đang chờ (PENDING).
     */
    @Transactional
    public UserDto approve(Actor actor, int id, boolean approved) {
        user(id);
        if (users.approvePending(id, approved ? "ACTIVE" : "REJECTED") != 1) {
            throw ApiException.conflict("Chỉ duyệt tài khoản đang chờ.");
        }
        tokens.revokeUser(id);
        audit.record(actor.userId(), approved ? "APPROVE" : "REJECT", "USER", id);
        return user(id);
    }

    /**
     * Đặt lại mật khẩu cho tài khoản và hủy toàn bộ phiên làm việc cũ.
     */
    @Transactional
    public void resetPassword(Actor actor, int id, String password) {
        user(id);
        users.setPassword(id, encode(password));
        tokens.revokeUser(id);
        audit.record(actor.userId(), "RESET_PASSWORD", "USER", id);
    }

    // ==========================================
    // 3. NGHIỆP VỤ PHÂN QUYỀN RBAC
    // ==========================================

    public List<RoleDto> roles() { 
        return roles.findAll(); 
    }

    public List<PermissionDto> permissions() { 
        return roles.permissions(); 
    }

    public List<PermissionDto> rolePermissions(int id) { 
        roles.findById(id);
        return roles.permissionsForRole(id); 
    }

    /**
     * Gán tập quyền mới cho một Role:
     * Đảm bảo Role Admin luôn giữ các quyền cốt lõi: MANAGE_RBAC, MANAGE_USERS, APPROVE_ACCOUNTS.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<PermissionDto> setPermissions(Actor actor, int roleId, Set<Integer> ids) {
        String role = roles.findById(roleId).roleName();
        Set<String> names = new HashSet<>();
        for (int id : ids) {
            names.add(roles.permission(id).permissionName());
        }
        if ("Admin".equals(role) && !names.containsAll(Set.of("MANAGE_RBAC", "MANAGE_USERS", "APPROVE_ACCOUNTS"))) {
            throw ApiException.conflict("Admin phải giữ quyền quản lý nhân sự, phân quyền và duyệt tài khoản.");
        }
        roles.replacePermissions(roleId, ids);
        audit.record(actor.userId(), "SET_PERMISSIONS", "ROLE", roleId);
        return rolePermissions(roleId);
    }

    // ==========================================
    // 4. NGHIỆP VỤ KHO VẬT TƯ (SUPPLY)
    // ==========================================

    public PageDto<SupplyDto> supplies(String search, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(supplies.search(search, skip, size), supplies.count(search), page, size);
    }

    public SupplyDto supply(int id) { 
        return supplies.findActiveById(id); 
    }

    /**
     * Thêm mới vật tư (Yêu cầu người quản lý phải là nhân sự đang hoạt động).
     */
    @Transactional
    public SupplyDto createSupply(Actor actor, SupplyInput input) {
        requireStaff(input.managedBy());
        int id = supplies.insert(input);
        audit.record(actor.userId(), "CREATE", "Supply", id);
        return supply(id);
    }

    /**
     * Cập nhật thông tin vật tư: Sử dụng version để kiểm tra xung đột ghi đè đồng thời.
     */
    @Transactional
    public SupplyDto updateSupply(Actor actor, int id, SupplyUpdate input) {
        supply(id);
        requireStaff(input.managedBy());
        if (supplies.update(id, input) != 1) throw staleStock();
        audit.record(actor.userId(), "UPDATE", "Supply", id);
        return supply(id);
    }

    /**
     * Điều chỉnh tồn kho (+ hoặc -): Kiểm tra số lượng khác 0, chống âm kho và kiểm tra version.
     */
    @Transactional
    public SupplyDto stock(Actor actor, int id, StockAdjustment input) {
        supply(id);
        if (input.delta() == 0) {
            throw new ApiException(400, "Số lượng điều chỉnh phải khác 0.");
        }
        if (supplies.adjustStock(id, input.delta(), input.version()) != 1) throw staleStock();
        audit.record(actor.userId(), "STOCK " + input.delta() + ": " + input.reason(), "Supply", id);
        return supply(id);
    }

    /**
     * Lưu trữ mặt hàng vật tư.
     */
    @Transactional
    public void archiveSupply(Actor actor, int id, int version) {
        supply(id);
        if (supplies.archive(id, version) != 1) throw staleStock();
        audit.record(actor.userId(), "ARCHIVE", "Supply", id);
    }

    // ==========================================
    // 5. NHẬT KÝ THAO TÁC (AUDIT LOGS)
    // ==========================================

    public PageDto<AuditLogDto> audit(Integer userId, String table, int page, int size) {
        int skip = offset(page, size);
        return PageDto.of(logs.search(userId, table, skip, size), logs.count(userId, table), page, size);
    }

    // ==========================================
    // CÁC HÀM HỖ TRỢ KIỂM TRA BẢO MẬT & HỢP LỆ
    // ==========================================

    /**
     * Kiểm tra bảo vệ Admin cuối cùng: Không cho phép khóa/hạ quyền nếu chỉ còn đúng 1 Admin hoạt động.
     */
    private void protectLastAdmin(UserDto user) {
        if ("Admin".equals(user.role()) && "ACTIVE".equals(user.status()) && users.countUsableAdmins() <= 1) {
            throw ApiException.conflict("Phải giữ ít nhất một Admin đang hoạt động có mật khẩu đăng nhập hợp lệ.");
        }
    }

    /**
     * Kiểm tra người dùng phải đang ACTIVE và mang vai trò chỉ định.
     */
    private void requireRole(int id, String role) {
        if (!users.isActiveWithRole(id, role)) {
            throw new ApiException(400, "Người dùng phải đang hoạt động và có vai trò " + role + ".");
        }
    }

    /**
     * Kiểm tra người phụ trách phải là nhân sự đang hoạt động (Admin, Groom, Trainer, Vet).
     */
    private void requireStaff(int id) {
        if (!users.isActiveStaff(id)) {
            throw new ApiException(400, "Người quản lý vật tư phải là nhân sự đang hoạt động.");
        }
    }

    private String encode(String password) {
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(400, "Mật khẩu tối đa 72 byte UTF-8.");
        }
        return passwords.encode(password);
    }

    private static void exists(int rows) { 
        if (rows == 0) throw ApiException.notFound(); 
    }

    private static ApiException staleStock() { 
        return ApiException.conflict("Vật tư vừa thay đổi hoặc số lượng tồn không hợp lệ. Hãy tải lại."); 
    }
}
