import { useState, type FormEvent } from "react";

/**
 * ============================================================================
 * FILE: FinanceInputForm.tsx
 * MỤC ĐÍCH: 
 *   - Khung form ghi nhận các bút toán tài chính (Khoản thu doanh thu hoặc Khoản chi chi phí).
 *   - Dành cho phân hệ Club Manager (Role: Admin) để theo dõi dòng tiền CLB.
 *   - Kết nối trực tiếp với API Backend: POST /api/manager/reports/finance/entries
 *   - Khớp 100% với DTO Backend: com.horsemanagement.dto.manager.Requests.FinanceInput
 * ============================================================================
 */

/** Dữ liệu bút toán tài chính gửi lên Backend */
export interface FinanceFormData {
  horseId?: number;                     // ID chú ngựa liên quan (Không bắt buộc)
  entryType: "INCOME" | "EXPENSE";      // Loại: INCOME (Khoản Thu) hoặc EXPENSE (Khoản Chi)
  amount: number;                       // Số tiền giao dịch tính bằng VNĐ (> 0)
  category: string;                     // Danh mục (Thức ăn, Thuốc thú y, Phí chuồng, Phí huấn luyện...)
  description: string;                  // Ghi chú chi tiết hóa đơn/chứng từ
  entryDate: string;                    // Ngày phát sinh giao dịch (YYYY-MM-DD, không được ở tương lai)
}

interface FinanceInputFormProps {
  onSubmit?: (data: FinanceFormData) => void | Promise<void>;
  isLoading?: boolean;
}

export function FinanceInputForm({ onSubmit, isLoading = false }: FinanceInputFormProps) {
  // State lưu dữ liệu form thu chi
  const [formData, setFormData] = useState<FinanceFormData>({
    entryType: "EXPENSE",
    amount: 1500000,
    category: "Thức ăn & Cỏ",
    description: "Nhập cỏ khô Alfalfa cho chuồng A",
    entryDate: new Date().toISOString().split("T")[0],
  });

  const [message, setMessage] = useState<string | null>(null);

  /** Hàm xử lý submit lưu bút toán */
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onSubmit) {
      await onSubmit(formData);
      setMessage("Đã ghi nhận bút toán tài chính thành công!");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Bút toán Thu / Chi</h3>
        <p className="text-sm text-zinc-500">Ghi nhận các khoản thu phí chuồng, phí huấn luyện hoặc chi phí thức ăn, thuốc thú y</p>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* Hàng: Loại giao dịch & Số tiền */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Loại giao dịch <span className="text-red-500">*</span>
          </label>
          <select
            value={formData.entryType}
            onChange={(e) => setFormData({ ...formData, entryType: e.target.value as "INCOME" | "EXPENSE" })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none font-semibold text-zinc-800 dark:text-zinc-200"
          >
            <option value="EXPENSE">Khoản CHI (Chi phí vận hành)</option>
            <option value="INCOME">Khoản THU (Doanh thu CLB)</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Số tiền (VNĐ) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            min="1000"
            step="1000"
            required
            value={formData.amount}
            onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || 0 })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>
      </div>

      {/* Hàng: Danh mục & Ngày ghi nhận */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Danh mục tài chính <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            required
            maxLength={100}
            value={formData.category}
            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            placeholder="Thức ăn, Thuốc men, Phí thuê chuồng..."
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
            Ngày ghi nhận <span className="text-red-500">*</span>
          </label>
          <input
            type="date"
            required
            value={formData.entryDate}
            onChange={(e) => setFormData({ ...formData, entryDate: e.target.value })}
            className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
          />
        </div>
      </div>

      {/* Ô nhập: Liên kết chú ngựa (tùy chọn) */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Mã ngựa liên quan (HorseID - Không bắt buộc)
        </label>
        <input
          type="number"
          min={1}
          value={formData.horseId ?? ""}
          onChange={(e) => setFormData({ ...formData, horseId: e.target.value ? parseInt(e.target.value) : undefined })}
          placeholder="Để trống nếu là chi phí chung của toàn CLB"
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Ô nhập: Diễn giải chi tiết */}
      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Diễn giải chi tiết
        </label>
        <textarea
          rows={2}
          maxLength={1000}
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          placeholder="Ghi chú hóa đơn, chứng từ hoặc mục đích khoản chi..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      {/* Nút gửi form */}
      <div className="pt-2 flex justify-end">
        <button
          type="submit"
          disabled={isLoading}
          className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? "Đang lưu..." : "Lưu bút toán"}
        </button>
      </div>
    </form>
  );
}
