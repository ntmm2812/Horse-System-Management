import { useState, useRef, type FormEvent, type ChangeEvent } from "react";

/**
 * ============================================================================
 * FILE: IncidentReportForm.tsx
 * MỤC ĐÍCH: 
 *   - Khung form báo cáo sự cố y tế hoặc chấn thương của ngựa kèm ảnh minh chứng.
 *   - Dành riêng cho phân hệ Nhân viên chuồng trại (Groom).
 *   - Kết nối trực tiếp với 2 API Backend: 
 *       1. POST /api/groom/incidents (Tạo báo cáo sự cố dạng JSON)
 *       2. POST /api/groom/incidents/{id}/image (Tải lên ảnh đính kèm dạng Multipart Form-Data)
 *   - Ràng buộc bảo mật phía Client & Server: File ảnh PNG/JPEG, dung lượng <= 5MB.
 *   - Khớp 100% với DTO Backend: com.horsemanagement.dto.groom.IncidentInput
 * ============================================================================
 */

/** Dữ liệu form báo cáo sự cố gửi lên Backend */
export interface IncidentFormData {
  horseId: number;          // ID chú ngựa gặp sự cố (Bắt buộc, > 0)
  incidentType: string;     // Phân loại sự cố (Chấn thương móng, Bỏ ăn, Đau bụng, Sốt...)
  description: string;      // Mô tả chi tiết triệu chứng (Tối đa 2000 ký tự)
  date: string;             // Ngày xảy ra (Không được chọn ngày tương lai)
  imageFile?: File;         // File ảnh chụp tại hiện trường (Tùy chọn, tối đa 5MB)
}

interface IncidentReportFormProps {
  onSubmit?: (data: IncidentFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function IncidentReportForm({ onSubmit, isLoading = false }: IncidentReportFormProps) {
  // State quản lý dữ liệu form
  const [formData, setFormData] = useState<IncidentFormData>({
    horseId: 1,
    incidentType: "Chấn thương chân/móng",
    description: "",
    date: new Date().toISOString().split("T")[0],
  });

  // State hiển thị ảnh xem trước (Preview)
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  /** Xử lý khi người dùng chọn file ảnh từ máy tính */
  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Kiểm tra dung lượng tối đa 5MB theo yêu cầu của Backend
      if (file.size > 5 * 1024 * 1024) {
        alert("File ảnh không được vượt quá 5MB.");
        return;
      }
      setFormData((prev) => ({ ...prev, imageFile: file }));
      // Đọc file để hiển thị preview ngay trên giao diện
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  /** Xử lý khi bấm nút gửi báo cáo */
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onSubmit) {
      await onSubmit(formData);
      setMessage("Báo cáo sự cố đã được gửi đến Bác sĩ thú y & Quản lý CLB!");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Báo cáo Sự cố (Groom)</h3>
        <p className="text-sm text-zinc-500">Ghi nhận chấn thương, bỏ ăn hoặc tình trạng sức khỏe bất thường của ngựa</p>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* Hàng: Mã ngựa & Ngày xảy ra */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Mã ngựa (HorseID) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            min={1}
            required
            value={formData.horseId}
            onChange={(e) => setFormData({ ...formData, horseId: parseInt(e.target.value) || 1 })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Ngày xảy ra sự cố <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            required
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>
      </div>

      {/* Dropdown: Phân loại sự cố */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Phân loại sự cố <span className="text-red-500">*</span>
        </label>
        <select
          value={formData.incidentType}
          onChange={(e) => setFormData({ ...formData, incidentType: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        >
          <option value="Chấn thương chân/móng">Chấn thương chân / móng</option>
          <option value="Ngựa bỏ ăn / Chán ăn">Ngựa bỏ ăn / Chán ăn</option>
          <option value="Sốt hoặc đau bụng (Colic)">Sốt hoặc đau bụng (Colic)</option>
          <option value="Trầy xước da / Vết thương ngoài">Trầy xước da / Vết thương ngoài</option>
          <option value="Hành vi hoảng loạn bất thường">Hành vi hoảng loạn bất thường</option>
          <option value="Sự cố chuồng trại / Hư hỏng rào">Sự cố chuồng trại / Hư hỏng rào</option>
        </select>
      </div>

      {/* Ô nhập: Mô tả chi tiết */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Mô tả chi tiết triệu chứng & tình trạng
        </label>
        <textarea
          rows={3}
          maxLength={2000}
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          placeholder="Mô tả vị trí vết thương, biểu hiện của ngựa, các sơ cứu ban đầu đã thực hiện..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Khung tải ảnh đính kèm */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Ảnh minh chứng sự cố (PNG / JPEG &le; 5MB)
        </label>
        <div className="flex items-center gap-4">
          <input
            type="file"
            ref={fileInputRef}
            accept="image/png,image/jpeg,image/jpg"
            onChange={handleImageChange}
            className="hidden"
          />
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="px-4 py-2 border border-dashed border-zinc-400 dark:border-zinc-600 rounded-lg text-sm text-zinc-600 dark:text-zinc-300 hover:border-amber-500 hover:text-amber-500 transition-colors"
          >
            {formData.imageFile ? "Chọn ảnh khác" : "Chọn file ảnh đính kèm"}
          </button>
          {formData.imageFile && (
            <span className="text-xs text-zinc-500">{formData.imageFile.name} ({(formData.imageFile.size / 1024).toFixed(0)} KB)</span>
          )}
        </div>

        {/* Khung xem trước ảnh */}
        {imagePreview && (
          <div className="mt-3 relative w-36 h-28 rounded-lg overflow-hidden border border-zinc-300 dark:border-zinc-700">
            <img src={imagePreview} alt="Preview sự cố" className="w-full h-full object-cover" />
            <button
              type="button"
              onClick={() => { setImagePreview(null); setFormData((prev) => ({ ...prev, imageFile: undefined })); }}
              className="absolute top-1 right-1 bg-red-600 text-white rounded-full p-1 text-xs"
              title="Xóa ảnh"
            >
              &times;
            </button>
          </div>
        )}
      </div>

      {/* Nút gửi báo cáo */}
      <div className="pt-2 flex justify-end">
        <button
          type="submit"
          disabled={isLoading}
          className="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? "Đang gửi báo cáo..." : "Gửi báo cáo sự cố khẩn cấp"}
        </button>
      </div>
    </form>
  );
}
