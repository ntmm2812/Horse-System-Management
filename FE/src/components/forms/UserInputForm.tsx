import { useState, type FormEvent } from "react";

/**
 * ============================================================================
 * FILE: UserInputForm.tsx
 * MỤC ĐÍCH: 
 *   - Khung form tạo tài khoản người dùng mới và phân quyền vai trò ban đầu.
 *   - Dành cho phân hệ Club Manager (Role: Admin) để tạo nhân sự (Groom, Trainer, Vet...).
 *   - Kết nối trực tiếp với API Backend: POST /api/manager/users
 *   - Khớp 100% với DTO Backend: com.horsemanagement.dto.manager.Requests.UserInput
 * ============================================================================
 */

/** Dữ liệu form người dùng gửi lên Backend */
export interface UserFormData {
  fullName: string;  // Họ và tên (Bắt buộc, tối đa 100 ký tự)
  email: string;     // Email đăng nhập (Bắt buộc, chuẩn định dạng email, tối đa 254 ký tự)
  password: string;  // Mật khẩu khởi tạo (Bắt buộc, từ 10 đến 72 ký tự)
  roleId: number;    // ID vai trò trong hệ thống (1: Admin, 2: Groom, 3: Trainer, 4: Vet, 5: Member)
}

interface UserInputFormProps {
  onCreateUser?: (data: UserFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function UserInputForm({ onCreateUser, isLoading = false }: UserInputFormProps) {
  // State lưu trữ dữ liệu form
  const [userData, setUserData] = useState<UserFormData>({
    fullName: "",
    email: "",
    password: "",
    roleId: 2, // Mặc định chọn tạo nhân viên Groom
  });

  const [message, setMessage] = useState<string | null>(null);

  /** Hàm xử lý gửi form tạo tài khoản */
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onCreateUser) {
      await onCreateUser(userData);
      setMessage(`Đã tạo tài khoản cho ${userData.fullName} thành công!`);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Nhân sự & Phân quyền</h3>
        <p className="text-sm text-zinc-500">Tạo tài khoản nhân sự mới và thiết lập vai trò ban đầu</p>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* Ô nhập: Họ và tên */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Họ và tên <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          required
          maxLength={100}
          value={userData.fullName}
          onChange={(e) => setUserData({ ...userData, fullName: e.target.value })}
          placeholder="Ví dụ: Trần Minh Khoa, Nguyễn Văn A..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Ô nhập: Email */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Địa chỉ Email đăng nhập <span className="text-red-500">*</span>
        </label>
        <input
          type="email"
          required
          maxLength={254}
          value={userData.email}
          onChange={(e) => setUserData({ ...userData, email: e.target.value })}
          placeholder="nhanvien@horseclub.com"
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Ô nhập: Mật khẩu khởi tạo */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Mật khẩu ban đầu (10 - 72 ký tự) <span className="text-red-500">*</span>
        </label>
        <input
          type="password"
          required
          minLength={10}
          maxLength={72}
          value={userData.password}
          onChange={(e) => setUserData({ ...userData, password: e.target.value })}
          placeholder="Tối thiểu 10 ký tự bảo mật..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Dropdown chọn: Vai trò hệ thống (Role) */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Vai trò hệ thống (Role) <span className="text-red-500">*</span>
        </label>
        <select
          value={userData.roleId}
          onChange={(e) => setUserData({ ...userData, roleId: parseInt(e.target.value) || 2 })}
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        >
          <option value={1}>Quản lý CLB (Admin / Club Manager)</option>
          <option value={2}>Nhân viên chăm sóc (Groom)</option>
          <option value={3}>Huấn luyện viên (Trainer)</option>
          <option value={4}>Bác sĩ thú y (Vet)</option>
          <option value={5}>Chủ sở hữu / Thành viên (Owner / Member)</option>
        </select>
      </div>

      {/* Nút gửi form */}
      <div className="pt-2 flex justify-end">
        <button
          type="submit"
          disabled={isLoading}
          className="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? "Đang tạo..." : "Tạo tài khoản nhân sự"}
        </button>
      </div>
    </form>
  );
}
