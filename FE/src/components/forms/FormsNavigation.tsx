import { useState } from "react";
import { HorseInputForm, type HorseFormData } from "./HorseInputForm";
import { SupplyInputForm, type SupplyFormData, type StockAdjustmentData } from "./SupplyInputForm";
import { UserInputForm, type UserFormData } from "./UserInputForm";
import { FinanceInputForm, type FinanceFormData } from "./FinanceInputForm";
import { IncidentReportForm, type IncidentFormData } from "./IncidentReportForm";
import { CareTaskForm, type CareTaskCompleteData } from "./CareTaskForm";

/**
 * ============================================================================
 * FILE: FormsNavigation.tsx
 * MỤC ĐÍCH:
 *   - File ĐIỀU HƯỚNG TRUNG TÂM (Navigation Hub) kết nối toàn bộ các khung nhập liệu.
 *   - Cho phép người dùng / đội Frontend / Giảng viên dễ dàng chuyển đổi qua lại
 *     giữa các biểu mẫu của Club Manager và Groom.
 *   - Tích hợp "Khung xem trước Payload": khi người dùng nhập bất kỳ form nào và bấm
 *     Submit, hệ thống sẽ hiển thị trực quan cấu trúc JSON, HTTP Method và Endpoint
 *     sẽ được gửi tới Backend Spring Boot.
 *   - Cung cấp tài liệu sống động và khung test thực tế cho đồ án SWP391.
 * ============================================================================
 */

export function FormsNavigation() {
  // State điều hướng: lưu form hiện tại đang được chọn hiển thị
  const [activeForm, setActiveForm] = useState<
    "horse" | "supply" | "user" | "finance" | "incident" | "careTask"
  >("horse");

  // State lưu lịch sử payload gửi đi để người dùng kiểm tra JSON
  const [apiLog, setApiLog] = useState<{
    endpoint: string;
    method: string;
    payload: any;
    timestamp: string;
  } | null>(null);

  /** Hàm bắt sự kiện submit Form Ngựa */
  const handleHorseSubmit = (data: HorseFormData) => {
    setApiLog({
      endpoint: "/api/manager/horses",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Thêm vật tư */
  const handleSupplySubmit = (data: SupplyFormData) => {
    setApiLog({
      endpoint: "/api/manager/supplies",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Điều chỉnh tồn kho */
  const handleStockAdjust = (data: StockAdjustmentData) => {
    setApiLog({
      endpoint: "/api/manager/supplies/{id}/stock",
      method: "PATCH",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Tạo nhân sự */
  const handleUserSubmit = (data: UserFormData) => {
    setApiLog({
      endpoint: "/api/manager/users",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Thu/Chi tài chính */
  const handleFinanceSubmit = (data: FinanceFormData) => {
    setApiLog({
      endpoint: "/api/manager/reports/finance/entries",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Báo cáo sự cố */
  const handleIncidentSubmit = (data: IncidentFormData) => {
    setApiLog({
      endpoint: "/api/groom/incidents (và upload ảnh tới /api/groom/incidents/{id}/image)",
      method: "POST (Multipart / Form-Data)",
      payload: {
        horseId: data.horseId,
        incidentType: data.incidentType,
        description: data.description,
        date: data.date,
        hasImage: Boolean(data.imageFile),
      },
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm bắt sự kiện submit Form Hoàn thành công việc Groom */
  const handleCareTaskSubmit = (data: CareTaskCompleteData) => {
    setApiLog({
      endpoint: `/api/groom/tasks/${data.taskId}/complete`,
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6 font-sans">
      {/* ===================== THANH ĐIỀU HƯỚNG CHÍNH ===================== */}
      <div className="border-b border-zinc-200 dark:border-zinc-800 pb-4">
        <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-100">
          Trung tâm Điều hướng & Khung nhập liệu (SWP391)
        </h1>
        <p className="text-sm text-zinc-500 mt-1">
          Tập hợp các biểu mẫu nhập thông tin chuẩn xác cho 2 phân hệ trọng tâm: <strong>Club Manager (Quản lý)</strong> và <strong>Groom (Chăm sóc ngựa)</strong>.
        </p>

        {/* Các nút bấm điều hướng chọn Form */}
        <div className="mt-4 flex flex-wrap gap-2">
          {/* Nhóm điều hướng Club Manager */}
          <span className="text-xs uppercase font-bold tracking-wider text-zinc-400 self-center mr-2">Manager:</span>
          <button
            onClick={() => setActiveForm("horse")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "horse"
                ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Hồ sơ Ngựa
          </button>
          <button
            onClick={() => setActiveForm("supply")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "supply"
                ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Kho Vật tư
          </button>
          <button
            onClick={() => setActiveForm("user")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "user"
                ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Nhân sự & Role
          </button>
          <button
            onClick={() => setActiveForm("finance")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "finance"
                ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Bút toán Thu/Chi
          </button>

          {/* Nhóm điều hướng Groom */}
          <span className="text-xs uppercase font-bold tracking-wider text-zinc-400 self-center ml-4 mr-2">Groom:</span>
          <button
            onClick={() => setActiveForm("incident")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "incident"
                ? "bg-red-600 text-white border-red-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Báo cáo Sự cố & Ảnh
          </button>
          <button
            onClick={() => setActiveForm("careTask")}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all ${
              activeForm === "careTask"
                ? "bg-emerald-600 text-white border-emerald-600 shadow-sm"
                : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            }`}
          >
            Hoàn thành việc Groom
          </button>
        </div>
      </div>

      {/* ===================== KHU VỰC HIỂN THỊ FORM & XEM TRƯỚC ===================== */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Cột 1 & 2: Hiển thị Khung Form tương ứng theo tab đang chọn */}
        <div className="lg:col-span-2">
          {activeForm === "horse" && <HorseInputForm onSubmit={handleHorseSubmit} />}
          {activeForm === "supply" && (
            <SupplyInputForm onAddSupply={handleSupplySubmit} onAdjustStock={handleStockAdjust} />
          )}
          {activeForm === "user" && <UserInputForm onCreateUser={handleUserSubmit} />}
          {activeForm === "finance" && <FinanceInputForm onSubmit={handleFinanceSubmit} />}
          {activeForm === "incident" && <IncidentReportForm onSubmit={handleIncidentSubmit} />}
          {activeForm === "careTask" && <CareTaskForm onCompleteTask={handleCareTaskSubmit} />}
        </div>

        {/* Cột 3: Khung kiểm tra Payload JSON & API Endpoint tương ứng */}
        <div className="bg-zinc-900 text-zinc-100 p-5 rounded-xl border border-zinc-800 space-y-4">
          <div className="flex justify-between items-center border-b border-zinc-800 pb-2">
            <h4 className="text-sm font-bold text-amber-400">Kiểm tra Payload gửi Backend</h4>
            {apiLog && <span className="text-xs text-zinc-500">{apiLog.timestamp}</span>}
          </div>

          {apiLog ? (
            <div className="space-y-3 text-xs">
              <div>
                <span className="text-zinc-400">HTTP Method:</span>
                <span className="ml-2 font-mono font-bold text-emerald-400">{apiLog.method}</span>
              </div>
              <div>
                <span className="text-zinc-400">API Endpoint:</span>
                <div className="font-mono bg-zinc-800 p-2 rounded text-amber-300 mt-1 break-all">
                  {apiLog.endpoint}
                </div>
              </div>
              <div>
                <span className="text-zinc-400">Dữ liệu JSON gửi đi (Payload):</span>
                <pre className="font-mono bg-zinc-950 p-2 rounded text-zinc-300 mt-1 overflow-x-auto max-h-64">
                  {JSON.stringify(apiLog.payload, null, 2)}
                </pre>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center text-zinc-500 text-xs">
              Nhập dữ liệu vào form bên trái và bấm lưu để xem trước cấu trúc JSON gửi lên Backend.
            </div>
          )}

          {/* Hướng dẫn tái sử dụng cho đội FE */}
          <div className="pt-3 border-t border-zinc-800 text-xs text-zinc-400 space-y-1">
            <p>💡 <strong>Hướng dẫn cho đội FE:</strong></p>
            <p>1. Có thể import và dùng trực tiếp: <code>&lt;FormsNavigation /&gt;</code>.</p>
            <p>2. Hoặc import riêng từng Form từ <code>FE/src/components/forms/</code>.</p>
            <p>3. Các trường input và validation đã chuẩn 100% với DTO của Backend Spring Boot.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default FormsNavigation;
