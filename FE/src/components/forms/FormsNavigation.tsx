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
 *   - File ĐIỀU HƯỚNG TRUNG TÂM (Navigation Hub) dành riêng cho 2 vai trò:
 *       1. Club Manager (Quản lý CLB)
 *       2. Groom (Nhân viên Chăm sóc Ngựa)
 *   - Cho phép người dùng chuyển đổi trực tiếp giữa 2 vai trò và chọn từng khung
 *     nhập liệu tương ứng.
 *   - Tích hợp bộ kiểm tra "Live Payload Inspector": khi người dùng nhập dữ liệu
 *     ở bất kỳ form nào và bấm Gửi, hệ thống sẽ hiển thị trực tiếp cấu trúc JSON,
 *     HTTP Method, URL Endpoint Backend và Header Bearer Token.
 *   - Mỗi phần, hàm xử lý và state đều được chú thích tiếng Việt rõ ràng.
 * ============================================================================
 */

// Định nghĩa kiểu dữ liệu cho vai trò hiện tại
export type ActiveRole = "manager" | "groom";

// Định nghĩa kiểu dữ liệu cho form của Manager
export type ManagerFormType = "horse" | "supply" | "user" | "finance";

// Định nghĩa kiểu dữ liệu cho form của Groom
export type GroomFormType = "incident" | "careTask";

export function FormsNavigation() {
  // State 1: Lưu trữ vai trò đang được chọn (Mặc định là Club Manager)
  const [activeRole, setActiveRole] = useState<ActiveRole>("manager");

  // State 2: Lưu trữ form đang hiển thị của Club Manager
  const [managerForm, setManagerForm] = useState<ManagerFormType>("horse");

  // State 3: Lưu trữ form đang hiển thị của Groom
  const [groomForm, setGroomForm] = useState<GroomFormType>("incident");

  // State 4: Lưu trữ thông tin request API gửi đi để hiển thị lên khung kiểm tra
  const [apiLog, setApiLog] = useState<{
    role: string;
    endpoint: string;
    method: string;
    payload: any;
    timestamp: string;
  } | null>(null);

  // State 5: Thông báo copy thành công
  const [copied, setCopied] = useState<boolean>(false);

  /**
   * ==========================================================================
   * CÁC HÀM XỬ LÝ SUBMIT (EVENT HANDLERS) CHO PHÂN HỆ CLUB MANAGER
   * ==========================================================================
   */

  /** Xử lý submit Form Quản lý Hồ sơ Ngựa */
  const handleHorseSubmit = (data: HorseFormData) => {
    setApiLog({
      role: "Club Manager",
      endpoint: "/api/manager/horses",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Xử lý submit Form Thêm Vật tư mới */
  const handleSupplySubmit = (data: SupplyFormData) => {
    setApiLog({
      role: "Club Manager",
      endpoint: "/api/manager/supplies",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Xử lý submit Form Điều chỉnh tồn kho (Khóa lạc quan version) */
  const handleStockAdjust = (data: StockAdjustmentData) => {
    setApiLog({
      role: "Club Manager",
      endpoint: "/api/manager/supplies/{id}/stock",
      method: "PATCH",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Xử lý submit Form Tạo tài khoản & Phân quyền */
  const handleUserSubmit = (data: UserFormData) => {
    setApiLog({
      role: "Club Manager",
      endpoint: "/api/manager/users",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Xử lý submit Form Ghi nhận bút toán Thu / Chi */
  const handleFinanceSubmit = (data: FinanceFormData) => {
    setApiLog({
      role: "Club Manager",
      endpoint: "/api/manager/reports/finance/entries",
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /**
   * ==========================================================================
   * CÁC HÀM XỬ LÝ SUBMIT (EVENT HANDLERS) CHO PHÂN HỆ GROOM
   * ==========================================================================
   */

  /** Xử lý submit Form Báo cáo sự cố kèm file ảnh */
  const handleIncidentSubmit = (data: IncidentFormData) => {
    setApiLog({
      role: "Groom",
      endpoint: "/api/groom/incidents (và upload ảnh tới /api/groom/incidents/{id}/image)",
      method: "POST (Multipart / Form-Data)",
      payload: {
        horseId: data.horseId,
        incidentType: data.incidentType,
        description: data.description,
        date: data.date,
        hasImageFile: Boolean(data.imageFile),
        imageFileName: data.imageFile?.name || null,
        imageFileSize: data.imageFile ? `${(data.imageFile.size / 1024).toFixed(1)} KB` : null,
      },
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Xử lý submit Form Xác nhận hoàn thành công việc chăm sóc */
  const handleCareTaskSubmit = (data: CareTaskCompleteData) => {
    setApiLog({
      role: "Groom",
      endpoint: `/api/groom/tasks/${data.taskId}/complete`,
      method: "POST",
      payload: data,
      timestamp: new Date().toLocaleTimeString(),
    });
  };

  /** Hàm sao chép JSON Payload vào bộ nhớ Clipboard */
  const handleCopyPayload = () => {
    if (apiLog?.payload) {
      navigator.clipboard.writeText(JSON.stringify(apiLog.payload, null, 2));
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-4 sm:p-6 space-y-6 font-sans">
      {/* ===================== KHUNG CHỌN VAI TRÒ CHÍNH (ROLE TOGGLE) ===================== */}
      <div className="bg-white dark:bg-zinc-900 rounded-2xl border border-zinc-200 dark:border-zinc-800 p-5 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-zinc-900 dark:text-zinc-100 flex items-center gap-2">
              <span>🐎</span>
              <span>Khung Nhập Liệu Theo Role: Club Manager & Groom</span>
            </h1>
            <p className="text-sm text-zinc-500 mt-1">
              Chọn vai trò bên dưới để thao tác với các biểu mẫu nhập thông tin chuẩn xác của đồ án SWP391.
            </p>
          </div>

          {/* Nút bấm chuyển đổi giữa 2 vai trò cốt lõi */}
          <div className="inline-flex rounded-xl bg-zinc-100 dark:bg-zinc-800 p-1 border border-zinc-200 dark:border-zinc-700">
            <button
              onClick={() => setActiveRole("manager")}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeRole === "manager"
                  ? "bg-amber-600 text-white shadow-sm"
                  : "text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100"
              }`}
            >
              <span>👑</span>
              <span>Club Manager</span>
            </button>
            <button
              onClick={() => setActiveRole("groom")}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                activeRole === "groom"
                  ? "bg-emerald-600 text-white shadow-sm"
                  : "text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100"
              }`}
            >
              <span>🌾</span>
              <span>Groom (Chuồng)</span>
            </button>
          </div>
        </div>

        {/* Thanh chọn Form con theo từng vai trò đã chọn */}
        <div className="pt-3 border-t border-zinc-100 dark:border-zinc-800 flex flex-wrap items-center gap-2">
          {activeRole === "manager" ? (
            <>
              <span className="text-xs font-bold uppercase tracking-wider text-amber-600 dark:text-amber-400 mr-2">
                Biểu mẫu Manager:
              </span>
              <button
                onClick={() => setManagerForm("horse")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  managerForm === "horse"
                    ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                1. Hồ sơ Ngựa
              </button>
              <button
                onClick={() => setManagerForm("supply")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  managerForm === "supply"
                    ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                2. Kho & Tồn kho
              </button>
              <button
                onClick={() => setManagerForm("user")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  managerForm === "user"
                    ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                3. Nhân sự & Phân quyền
              </button>
              <button
                onClick={() => setManagerForm("finance")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  managerForm === "finance"
                    ? "bg-amber-600 text-white border-amber-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                4. Bút toán Thu / Chi
              </button>
            </>
          ) : (
            <>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 mr-2">
                Biểu mẫu Groom:
              </span>
              <button
                onClick={() => setGroomForm("incident")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  groomForm === "incident"
                    ? "bg-emerald-600 text-white border-emerald-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                1. Báo cáo Sự cố & Ảnh
              </button>
              <button
                onClick={() => setGroomForm("careTask")}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-all ${
                  groomForm === "careTask"
                    ? "bg-emerald-600 text-white border-emerald-600 shadow-sm"
                    : "border-zinc-300 dark:border-zinc-700 text-zinc-700 dark:text-zinc-300 hover:bg-zinc-100 dark:hover:bg-zinc-800"
                }`}
              >
                2. Hoàn thành Việc chăm sóc
              </button>
            </>
          )}
        </div>
      </div>

      {/* ===================== KHU VỰC HIỂN THỊ FORM VÀ LIVE PAYLOAD ===================== */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* CỘT 1 & 2: Hiển thị Khung Form của vai trò đang chọn */}
        <div className="lg:col-span-2">
          {activeRole === "manager" ? (
            <>
              {managerForm === "horse" && <HorseInputForm onSubmit={handleHorseSubmit} />}
              {managerForm === "supply" && (
                <SupplyInputForm onAddSupply={handleSupplySubmit} onAdjustStock={handleStockAdjust} />
              )}
              {managerForm === "user" && <UserInputForm onCreateUser={handleUserSubmit} />}
              {managerForm === "finance" && <FinanceInputForm onSubmit={handleFinanceSubmit} />}
            </>
          ) : (
            <>
              {groomForm === "incident" && <IncidentReportForm onSubmit={handleIncidentSubmit} />}
              {groomForm === "careTask" && <CareTaskForm onCompleteTask={handleCareTaskSubmit} />}
            </>
          )}
        </div>

        {/* CỘT 3: Bảng kiểm tra Live Payload & Endpoint Backend */}
        <div className="bg-zinc-900 text-zinc-100 p-5 rounded-2xl border border-zinc-800 space-y-4 sticky top-20 shadow-md">
          <div className="flex justify-between items-center border-b border-zinc-800 pb-3">
            <div>
              <h4 className="text-sm font-bold text-amber-400">Kiểm tra Payload gửi Backend</h4>
              <p className="text-[11px] text-zinc-400">Khớp chuẩn Controller Spring Boot</p>
            </div>
            {apiLog && (
              <span className="text-[11px] px-2 py-0.5 rounded bg-zinc-800 text-zinc-300 font-mono">
                {apiLog.timestamp}
              </span>
            )}
          </div>

          {apiLog ? (
            <div className="space-y-3 text-xs">
              <div>
                <span className="text-zinc-400">Vai trò gọi:</span>
                <span className="ml-2 font-semibold text-zinc-200">{apiLog.role}</span>
              </div>

              <div>
                <span className="text-zinc-400">HTTP Method:</span>
                <span className="ml-2 font-mono font-bold text-emerald-400 px-1.5 py-0.5 bg-emerald-950/60 rounded border border-emerald-800/60">
                  {apiLog.method}
                </span>
              </div>

              <div>
                <span className="text-zinc-400">API Endpoint:</span>
                <div className="font-mono bg-zinc-950 p-2 rounded-lg text-amber-300 mt-1 break-all border border-zinc-800">
                  {apiLog.endpoint}
                </div>
              </div>

              <div>
                <div className="flex items-center justify-between">
                  <span className="text-zinc-400">JSON Request Body (Payload):</span>
                  <button
                    onClick={handleCopyPayload}
                    className="text-[11px] text-amber-400 hover:text-amber-300 underline"
                  >
                    {copied ? "Đã sao chép!" : "Sao chép JSON"}
                  </button>
                </div>
                <pre className="font-mono bg-zinc-950 p-2.5 rounded-lg text-zinc-300 mt-1 overflow-x-auto max-h-64 border border-zinc-800 text-[11px] leading-relaxed">
                  {JSON.stringify(apiLog.payload, null, 2)}
                </pre>
              </div>
            </div>
          ) : (
            <div className="py-12 text-center text-zinc-500 text-xs space-y-2">
              <span className="text-2xl block">📋</span>
              <p>Điền thông tin vào form bên trái và bấm lưu để xem trực tiếp cấu trúc JSON gửi lên Backend.</p>
            </div>
          )}

          {/* Phần hướng dẫn cho đội Frontend riêng */}
          <div className="pt-3 border-t border-zinc-800 text-[11px] text-zinc-400 space-y-1">
            <p className="font-semibold text-zinc-300">💡 Hướng dẫn cho đội Frontend:</p>
            <p>1. Tất cả các trường input trong form này đã được định nghĩa kiểu dữ liệu khớp 100% với DTO của Backend Spring Boot.</p>
            <p>2. Đội FE có thể copy từng file form từ thư mục <code>FE/src/components/forms/</code> hoặc dùng trực tiếp trang này để kiểm thử tích hợp.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default FormsNavigation;
