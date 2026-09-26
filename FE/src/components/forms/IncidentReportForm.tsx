import { useState, useRef, type FormEvent, type ChangeEvent } from "react";

/**
 * Khung form nhập liệu: Báo cáo Sự cố & Tải ảnh đính kèm
 * Dùng cho phân hệ Nhân viên chuồng (Role: Groom)
 * Tương ứng API Backend: POST /api/groom/incidents, POST /api/groom/incidents/{id}/image
 * DTO Backend: com.horsemanagement.dto.groom.IncidentInput
 */
export interface IncidentFormData {
  horseId: number;
  incidentType: string;
  description: string;
  date: string;
  imageFile?: File;
}

interface IncidentReportFormProps {
  onSubmit?: (data: IncidentFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function IncidentReportForm({ onSubmit, isLoading = false }: IncidentReportFormProps) {
  const [formData, setFormData] = useState<IncidentFormData>({
    horseId: 1,
    incidentType: "Chấn thương nhẹ",
    description: "",
    date: new Date().toISOString().split("T")[0],
  });

  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        alert("File ảnh không được vượt quá 5MB.");
        return;
      }
      setFormData((prev) => ({ ...prev, imageFile: file }));
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

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

      {/* Mã ngựa & Ngày xảy ra */}
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

      {/* Loại sự cố */}
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

      {/* Mô tả chi tiết */}
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

      {/* Đính kèm ảnh */}
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

        {imagePreview && (
          <div className="mt-3 relative w-36 h-28 rounded-lg overflow-hidden border border-zinc-300 dark:border-zinc-700">
            <img src={imagePreview} alt="Preview sự cố" className="w-full h-full object-cover" />
            <button
              type="button"
              onClick={() => { setImagePreview(null); setFormData((prev) => ({ ...prev, imageFile: undefined })); }}
              className="absolute top-1 right-1 bg-red-600 text-white rounded-full p-1 text-xs"
            >
              &times;
            </button>
          </div>
        )}
      </div>

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
