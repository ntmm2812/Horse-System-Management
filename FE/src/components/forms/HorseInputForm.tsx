import { useState, type FormEvent } from "react";

/**
 * ============================================================================
 * FILE: HorseInputForm.tsx
 * MỤC ĐÍCH: 
 *   - Khung form nhập liệu cho việc "Thêm mới" hoặc "Chỉnh sửa" hồ sơ một chú ngựa.
 *   - Dành riêng cho phân hệ Quản lý chuồng ngựa (Club Manager - Role: Admin).
 *   - Kết nối trực tiếp với API Backend: 
 *       + POST /api/manager/horses (Thêm ngựa mới)
 *       + PUT /api/manager/horses/{id} (Cập nhật thông tin ngựa)
 *   - Khớp 100% với DTO Backend: com.horsemanagement.dto.manager.Requests.HorseInput
 * ============================================================================
 */

/** Cấu trúc dữ liệu của Form Ngựa (Khớp các trường Backend yêu cầu) */
export interface HorseFormData {
  name: string;      // Tên chú ngựa (Bắt buộc, tối đa 100 ký tự)
  gender: string;    // Giới tính: Stallion (Ngựa đực giống), Mare (Ngựa cái), Gelding (Ngựa thiến)
  age: number;       // Tuổi của ngựa (Số nguyên không âm >= 0)
  weight: number;    // Cân nặng tính bằng kg (Thập phân > 0.01)
  lineage: string;   // Dòng dõi, gia phả bố mẹ (Tối đa 255 ký tự)
  ownerId: number;   // ID của chủ sở hữu (UserID của tài khoản Owner/Member)
}

/** Props truyền vào Form từ Component cha */
interface HorseInputFormProps {
  initialData?: Partial<HorseFormData>;                      // Dữ liệu ban đầu (dùng khi mở form Sửa)
  onSubmit?: (data: HorseFormData) => void | Promise<void>;  // Hàm xử lý gửi dữ liệu khi bấm Submit
  isLoading?: boolean;                                       // Trạng thái đang tải / đang gọi API
}

export function HorseInputForm({ initialData, onSubmit, isLoading = false }: HorseInputFormProps) {
  // State quản lý toàn bộ dữ liệu người dùng nhập trên form
  const [formData, setFormData] = useState<HorseFormData>({
    name: initialData?.name || "",
    gender: initialData?.gender || "Stallion",
    age: initialData?.age ?? 4,
    weight: initialData?.weight ?? 450.0,
    lineage: initialData?.lineage || "",
    ownerId: initialData?.ownerId ?? 1,
  });

  // State hiển thị thông báo thành công hoặc lỗi
  const [message, setMessage] = useState<string | null>(null);

  /** Hàm xử lý khi người dùng nhấn nút Lưu / Submit form */
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onSubmit) {
      await onSubmit(formData);
      setMessage("Dữ liệu hồ sơ ngựa đã được gửi thành công!");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      {/* Tiêu đề & Hướng dẫn */}
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Hồ sơ Ngựa</h3>
        <p className="text-sm text-zinc-500">Nhập đầy đủ các trường thông tin theo quy chuẩn của Backend</p>
      </div>

      {/* Thông báo kết quả gửi form */}
      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* Ô nhập: Tên ngựa (name) */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Tên ngựa <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          required
          maxLength={100}
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          placeholder="Ví dụ: Xích Thố, Bạch Long Mã..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm focus:ring-2 focus:ring-amber-500 outline-none"
        />
      </div>

      {/* Hàng: Giới tính (gender) & Tuổi (age) */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Giới tính <span className="text-red-500">*</span>
          </label>
          <select
            value={formData.gender}
            onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          >
            <option value="Stallion">Ngựa đực giống (Stallion)</option>
            <option value="Mare">Ngựa cái (Mare)</option>
            <option value="Gelding">Ngựa thiến (Gelding)</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Tuổi (năm) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            min={0}
            max={40}
            required
            value={formData.age}
            onChange={(e) => setFormData({ ...formData, age: parseInt(e.target.value) || 0 })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>
      </div>

      {/* Hàng: Cân nặng (weight) & Mã chủ sở hữu (ownerId) */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Cân nặng (kg) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            required
            value={formData.weight}
            onChange={(e) => setFormData({ ...formData, weight: parseFloat(e.target.value) || 0 })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Mã chủ sở hữu (OwnerID) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            min={1}
            required
            value={formData.ownerId}
            onChange={(e) => setFormData({ ...formData, ownerId: parseInt(e.target.value) || 1 })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>
      </div>

      {/* Ô nhập: Dòng dõi / Gia phả (lineage) */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Dòng dõi / Gia phả (Lineage)
        </label>
        <textarea
          rows={2}
          maxLength={255}
          value={formData.lineage}
          onChange={(e) => setFormData({ ...formData, lineage: e.target.value })}
          placeholder="Ví dụ: Giống thuần chủng Ả Rập, đời F1..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Nút gửi dữ liệu (Submit Button) */}
      <div className="pt-2 flex justify-end gap-3">
        <button
          type="submit"
          disabled={isLoading}
          className="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? "Đang lưu..." : "Lưu thông tin ngựa"}
        </button>
      </div>
    </form>
  );
}
