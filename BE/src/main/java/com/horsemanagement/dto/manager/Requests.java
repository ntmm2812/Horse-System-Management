package com.horsemanagement.dto.manager;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Tổng hợp các DTO dữ liệu đầu vào cho nhóm chức năng Quản lý (Manager / Admin):
 * - Quản lý ngựa (HorseInput)
 * - Quản lý người dùng & phân quyền (UserInput, UserUpdate, StatusInput, Approval, ResetPassword, PermissionInput)
 * - Quản lý kho vật tư (SupplyInput, SupplyUpdate, StockAdjustment)
 * - Quản lý tài chính (FinanceInput, EntryType)
 */
public final class Requests {
    private Requests() {}

    /**
     * Dữ liệu thêm mới hoặc sửa thông tin chú ngựa.
     */
    public record HorseInput(
        @NotBlank(message = "Tên ngựa không được để trống")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Giới tính không được để trống")
        @Size(max = 20)
        String gender,

        @NotNull(message = "Tuổi không được để trống")
        @Min(value = 0, message = "Tuổi không thể âm")
        Integer age,

        @NotNull(message = "Cân nặng không được để trống")
        @DecimalMin("0.01")
        @Digits(integer = 5, fraction = 2)
        BigDecimal weight,

        @Size(max = 255)
        String lineage,

        @NotNull(message = "Chủ sở hữu không được để trống")
        @Positive(message = "ID chủ sở hữu phải là số dương")
        Integer ownerId
    ) {}

    /**
     * Dữ liệu tạo mới người dùng bởi Quản lý.
     */
    public record UserInput(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 254)
        String email,

        @NotNull(message = "Vai trò không được để trống")
        @Positive
        Integer roleId,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 10, max = 72, message = "Mật khẩu phải từ 10 đến 72 ký tự")
        String password
    ) {}

    /**
     * Dữ liệu cập nhật thông tin người dùng.
     */
    public record UserUpdate(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 254)
        String email,

        @NotNull(message = "Vai trò không được để trống")
        @Positive
        Integer roleId
    ) {}

    /**
     * Trạng thái tài khoản người dùng: ACTIVE (Hoạt động) hoặc DISABLED (Vô hiệu hóa).
     */
    public enum AccountState { ACTIVE, DISABLED }

    /**
     * Dữ liệu cập nhật trạng thái tài khoản.
     */
    public record StatusInput(@NotNull AccountState status) {}

    /**
     * Dữ liệu phê duyệt yêu cầu đăng ký (true = Chấp nhận / ACTIVE, false = Từ chối / REJECTED).
     */
    public record Approval(@NotNull Boolean approved) {}

    /**
     * Dữ liệu đặt lại mật khẩu cho tài khoản.
     */
    public record ResetPassword(
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 10, max = 72, message = "Mật khẩu phải từ 10 đến 72 ký tự")
        String password
    ) {}

    /**
     * Danh sách các ID quyền hạn gán cho một vai trò.
     */
    public record PermissionInput(
        @NotNull
        @Size(max = 100)
        Set<@NotNull @Positive Integer> permissionIds
    ) {}

    /**
     * Dữ liệu thêm mới vật tư vào kho.
     */
    public record SupplyInput(
        @NotBlank(message = "Tên vật tư không được để trống")
        @Size(max = 150)
        String itemName,

        @NotBlank(message = "Loại vật tư không được để trống")
        @Size(max = 50)
        String type,

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Min(value = 0, message = "Số lượng không thể âm")
        Integer quantityInStock,

        @NotNull(message = "Người quản lý không được để trống")
        @Positive
        Integer managedBy
    ) {}

    /**
     * Dữ liệu cập nhật thông tin vật tư (kèm version để khóa lạc quan).
     */
    public record SupplyUpdate(
        @NotBlank(message = "Tên vật tư không được để trống")
        @Size(max = 150)
        String itemName,

        @NotBlank(message = "Loại vật tư không được để trống")
        @Size(max = 50)
        String type,

        @NotNull(message = "Người quản lý không được để trống")
        @Positive
        Integer managedBy,

        @NotNull(message = "Phiên bản version không được để trống")
        @Min(0)
        Integer version
    ) {}

    /**
     * Dữ liệu điều chỉnh số lượng tồn kho (nhập/xuất kho).
     */
    public record StockAdjustment(
        @NotNull(message = "Độ biến thiên tồn kho delta không được để trống")
        Integer delta,

        @NotNull(message = "Phiên bản version không được để trống")
        @Min(0)
        Integer version,

        @NotBlank(message = "Lý do điều chỉnh không được để trống")
        @Size(max = 60)
        String reason
    ) {}

    /**
     * Loại bút toán tài chính: INCOME (Khoản thu) hoặc EXPENSE (Khoản chi).
     */
    public enum EntryType { INCOME, EXPENSE }

    /**
     * Dữ liệu thêm mới bút toán tài chính.
     */
    public record FinanceInput(
        @Positive(message = "Mã ngựa phải là số dương")
        Integer horseId,

        @NotNull(message = "Loại giao dịch không được để trống")
        EntryType entryType,

        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
        @Digits(integer = 16, fraction = 2)
        BigDecimal amount,

        @NotBlank(message = "Danh mục không được để trống")
        @Size(max = 100)
        String category,

        @Size(max = 1000)
        String description,

        @NotNull(message = "Ngày ghi nhận không được để trống")
        @PastOrPresent(message = "Ngày ghi nhận không thể ở tương lai")
        LocalDate entryDate
    ) {}
}
