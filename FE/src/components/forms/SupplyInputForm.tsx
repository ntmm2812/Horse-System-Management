import { useState, type FormEvent } from "react";

/**
 * ============================================================================
 * FILE: SupplyInputForm.tsx
 * MỤC ĐÍCH: 
 *   - Khung form quản lý kho vật tư và thức ăn chuồng ngựa.
 *   - Gồm 2 chức năng chính:
 *       1. Thêm mới mặt hàng vật tư vào kho (POST /api/manager/supplies)
 *       2. Điều chỉnh số lượng nhập/xuất kho (PATCH /api/manager/supplies/{id}/stock)
 *   - Tích hợp cơ chế khóa lạc quan (Optimistic Locking) qua trường Version để chống
 *     xung đột khi 2 người cùng nhập/xuất kho một lúc.
 *   - Khớp DTO Backend: Requests.SupplyInput và Requests.StockAdjustment
 * ============================================================================
 */

/** Cấu trúc dữ liệu khi tạo mới vật tư */
export interface SupplyFormData {
  itemName: string;         // Tên vật tư (Thức ăn, Yên cương, Dược phẩm...)
  type: string;             // Phân loại vật tư
  quantityInStock: number;  // Số lượng tồn kho ban đầu (>= 0)
  managedBy: number;        // ID người quản lý chịu trách nhiệm
}

/** Cấu trúc dữ liệu khi điều chỉnh tồn kho (Nhập/Xuất) */
export interface StockAdjustmentData {
  delta: number;    // Số lượng thay đổi (dương: nhập thêm, âm: xuất kho)
  version: number;  // Phiên bản version hiện tại trong DB để khóa lạc quan
  reason: string;   // Lý do điều chỉnh (Nhập hàng định kỳ, Hỏng hóc...)
}

/** Props nhận từ component cha */
interface SupplyInputFormProps {
  onAddSupply?: (data: SupplyFormData) => void | Promise<void>;
  onAdjustStock?: (data: StockAdjustmentData) => void | Promise<void>;
  isLoading?: boolean;
}

export function SupplyInputForm({ onAddSupply, onAdjustStock, isLoading = false }: SupplyInputFormProps) {
  // Quản lý tab đang hiển thị: "create" (thêm mới) hoặc "adjust" (nhập/xuất kho)
  const [activeTab, setActiveTab] = useState<"create" | "adjust">("create");

  // State dữ liệu form thêm mới
  const [supplyData, setSupplyData] = useState<SupplyFormData>({
    itemName: "",
    type: "Thức ăn",
    quantityInStock: 50,
    managedBy: 1,
  });

  // State dữ liệu form điều chỉnh tồn kho
  const [adjustData, setAdjustData] = useState<StockAdjustmentData>({
    delta: 10,
    version: 0,
    reason: "Nhập hàng định kỳ",
  });

  const [message, setMessage] = useState<string | null>(null);

  /** Xử lý submit thêm vật tư mới */
  const handleCreateSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onAddSupply) {
      await onAddSupply(supplyData);
      setMessage("Đã thêm vật tư mới vào kho thành công!");
    }
  };

  /** Xử lý submit điều chỉnh tồn kho */
  const handleAdjustSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onAdjustStock) {
      await onAdjustStock(adjustData);
      setMessage("Đã điều chỉnh tồn kho thành công!");
    }
  };

  return (
    <div className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      {/* Header & Thanh chuyển Tab */}
      <div className="flex justify-between items-center border-b border-zinc-200 dark:border-zinc-800 pb-3">
        <div>
          <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Kho Vật tư</h3>
          <p className="text-sm text-zinc-500">Quản lý thêm mới vật tư hoặc điều chỉnh nhập/xuất kho</p>
        </div>
        <div className="flex gap-1 bg-zinc-100 dark:bg-zinc-800 p-1 rounded-lg text-xs font-medium">
          <button
            type="button"
            onClick={() => { setActiveTab("create"); setMessage(null); }}
            className={`px-3 py-1.5 rounded-md transition-colors ${activeTab === "create" ? "bg-white dark:bg-zinc-700 shadow-sm text-amber-600 dark:text-amber-400" : "text-zinc-500"}`}
          >
            Thêm mới
          </button>
          <button
            type="button"
            onClick={() => { setActiveTab("adjust"); setMessage(null); }}
            className={`px-3 py-1.5 rounded-md transition-colors ${activeTab === "adjust" ? "bg-white dark:bg-zinc-700 shadow-sm text-amber-600 dark:text-amber-400" : "text-zinc-500"}`}
          >
            Nhập/Xuất kho
          </button>
        </div>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      {/* ===================== TAB 1: TẠO MỚI VẬT TƯ ===================== */}
      {activeTab === "create" ? (
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
              Tên vật tư <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={150}
              value={supplyData.itemName}
              onChange={(e) => setSupplyData({ ...supplyData, itemName: e.target.value })}
              placeholder="Ví dụ: Cỏ khô Alfalfa, Yên cương da, Vitamin C..."
              className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
                Phân loại <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                required
                maxLength={50}
                value={supplyData.type}
                onChange={(e) => setSupplyData({ ...supplyData, type: e.target.value })}
                placeholder="Thức ăn, Thiết bị, Thuốc..."
                className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
                Số lượng ban đầu <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min={0}
                required
                value={supplyData.quantityInStock}
                onChange={(e) => setSupplyData({ ...supplyData, quantityInStock: parseInt(e.target.value) || 0 })}
                className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
              Mã quản lý phụ trách (ManagedBy) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              min={1}
              required
              value={supplyData.managedBy}
              onChange={(e) => setSupplyData({ ...supplyData, managedBy: parseInt(e.target.value) || 1 })}
              className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
            />
          </div>

          <div className="pt-2 flex justify-end">
            <button
              type="submit"
              disabled={isLoading}
              className="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
            >
              {isLoading ? "Đang xử lý..." : "Thêm mới vật tư"}
            </button>
          </div>
        </form>
      ) : (
        /* ===================== TAB 2: ĐIỀU CHỈNH TỒN KHO ===================== */
        <form onSubmit={handleAdjustSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
                Số lượng thay đổi (Delta) <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                required
                value={adjustData.delta}
                onChange={(e) => setAdjustData({ ...adjustData, delta: parseInt(e.target.value) || 0 })}
                placeholder="+10 nhập kho, -5 xuất kho"
                className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
              />
              <span className="text-xs text-zinc-400">Dương: Nhập kho (+), Âm: Xuất kho (-)</span>
            </div>

            <div>
              <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
                Phiên bản Version hiện tại <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                min={0}
                required
                value={adjustData.version}
                onChange={(e) => setAdjustData({ ...adjustData, version: parseInt(e.target.value) || 0 })}
                className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
              />
              <span className="text-xs text-zinc-400">Khóa lạc quan chống xung đột sửa đổi</span>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
              Lý do điều chỉnh kho <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={60}
              value={adjustData.reason}
              onChange={(e) => setAdjustData({ ...adjustData, reason: e.target.value })}
              placeholder="Nhập kho từ nhà cung cấp, Sử dụng cho ô chuồng A..."
              className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
            />
          </div>

          <div className="pt-2 flex justify-end">
            <button
              type="submit"
              disabled={isLoading}
              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
            >
              {isLoading ? "Đang xử lý..." : "Cập nhật tồn kho"}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
