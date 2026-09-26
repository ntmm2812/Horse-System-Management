import { useState, type FormEvent } from "react";

/**
 * Khung form nhập liệu: Thêm mới hoặc Cập nhật hồ sơ Ngựa
 * Dùng cho phân hệ Club Manager (Role: Admin)
 * Tương ứng API Backend: POST /api/manager/horses, PUT /api/manager/horses/{id}
 * DTO Backend: com.horsemanagement.dto.manager.Requests.HorseInput
 */
export interface HorseFormData {
  name: string;
  gender: string;
  age: number;
  weight: number;
  lineage: string;
  ownerId: number;
}

interface HorseInputFormProps {
  initialData?: Partial<HorseFormData>;
  onSubmit?: (data: HorseFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function HorseInputForm({ initialData, onSubmit, isLoading = false }: HorseInputFormProps) {
  const [formData, setFormData] = useState<HorseFormData>({
    name: initialData?.name || "",
    gender: initialData?.gender || "Stallion",
    age: initialData?.age ?? 4,
    weight: initialData?.weight ?? 450.0,
    lineage: initialData?.lineage || "",
    ownerId: initialData?.ownerId ?? 1,
  });

  const [message, setMessage] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onSubmit) {
      await onSubmit(formData);
      setMessage("Dữ liệu hồ sơ ngựa đã được gửi thành công!");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Hồ sơ Ngựa</h3>
        <p className="text-sm text-zinc-500">Nhập đầy đủ các trường thông tin theo quy chuẩn của Backend</p>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* Tên ngựa */}
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

      {/* Giới tính & Tuổi */}
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

      {/* Cân nặng & Chủ sở hữu */}
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

      {/* Dòng dõi / Gia phả */}
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

      {/* Nút gửi dữ liệu */}
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
