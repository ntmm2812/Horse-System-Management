import { useState, type FormEvent } from "react";

/**
 * Khung form nhập liệu: Vật tư & Điều chỉnh tồn kho
 * Dùng cho phân hệ Club Manager (Role: Admin)
 * Tương ứng API Backend: POST /api/manager/supplies, PATCH /api/manager/supplies/{id}/stock
 * DTO Backend: com.horsemanagement.dto.manager.Requests.SupplyInput / StockAdjustment
 */
export interface SupplyFormData {
  itemName: string;
  type: string;
  quantityInStock: number;
  managedBy: number;
}

export interface StockAdjustmentData {
  delta: number;
  version: number;
  reason: string;
}

interface SupplyInputFormProps {
  onAddSupply?: (data: SupplyFormData) => void | Promise<void>;
  onAdjustStock?: (data: StockAdjustmentData) => void | Promise<void>;
  isLoading?: boolean;
}

export function SupplyInputForm({ onAddSupply, onAdjustStock, isLoading = false }: SupplyInputFormProps) {
  const [activeTab, setActiveTab] = useState<"create" | "adjust">("create");

  // Form Thêm vật tư mới
  const [supplyData, setSupplyData] = useState<SupplyFormData>({
    itemName: "",
    type: "Thức ăn",
    quantityInStock: 50,
    managedBy: 1,
  });

  // Form Điều chỉnh tồn kho (Nhập / Xuất)
  const [adjustData, setAdjustData] = useState<StockAdjustmentData>({
    delta: 10,
    version: 0,
    reason: "Nhập hàng định kỳ",
  });

  const [message, setMessage] = useState<string | null>(null);

  const handleCreateSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onAddSupply) {
      await onAddSupply(supplyData);
      setMessage("Đã thêm vật tư mới vào kho thành công!");
    }
  };

  const handleAdjustSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onAdjustStock) {
      await onAdjustStock(adjustData);
      setMessage("Đã điều chỉnh tồn kho thành công!");
    }
  };

  return (
    <div className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
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

      {/* Tab 1: Tạo mới vật tư */}
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
        /* Tab 2: Điều chỉnh tồn kho (Khóa lạc quan version) */
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
