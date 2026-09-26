import { useState, type FormEvent } from "react";

/**
 * Khung form nhập liệu: Tạo tài khoản nhân sự & Phân quyền
 * Dùng cho phân hệ Club Manager (Role: Admin)
 * Tương ứng API Backend: POST /api/manager/users, POST /api/manager/users/{id}/approve
 * DTO Backend: com.horsemanagement.dto.manager.Requests.UserInput / Approval
 */
export interface UserFormData {
  fullName: string;
  email: string;
  password: string;
  roleId: number;
}

interface UserInputFormProps {
  onCreateUser?: (data: UserFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function UserInputForm({ onCreateUser, isLoading = false }: UserInputFormProps) {
  const [userData, setUserData] = useState<UserFormData>({
    fullName: "",
    email: "",
    password: "",
    roleId: 2, // 1: Admin, 2: Groom, 3: Trainer, 4: Vet, 5: Member/Owner
  });

  const [message, setMessage] = useState<string | null>(null);

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

      {/* Họ và tên */}
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

      {/* Email */}
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

      {/* Mật khẩu khởi tạo */}
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

      {/* Vai trò */}
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
