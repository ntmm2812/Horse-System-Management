import { createFileRoute } from "@tanstack/react-router";
import { FormsNavigation } from "@/components/forms/FormsNavigation";

/**
 * ============================================================================
 * FILE: forms.tsx
 * ĐƯỜNG DẪN TRUY CẬP: /forms
 * MỤC ĐÍCH:
 *   - Trang chuyên biệt hiển thị toàn bộ khung nhập liệu (Form Skeletons)
 *     theo đúng 2 vai trò nghiệp vụ trọng tâm:
 *       1. Club Manager (Quản lý CLB):
 *          + Quản lý hồ sơ ngựa (HorseInputForm) -> POST /api/manager/horses
 *          + Quản lý kho & tồn kho (SupplyInputForm) -> POST /api/manager/supplies
 *          + Quản lý tài khoản & phân quyền (UserInputForm) -> POST /api/manager/users
 *          + Bút toán thu / chi tài chính (FinanceInputForm) -> POST /api/manager/reports/finance/entries
 *       2. Groom (Nhân viên Chăm sóc Ngựa):
 *          + Báo cáo sự cố y tế & đính kèm ảnh (IncidentReportForm) -> POST /api/groom/incidents
 *          + Xác nhận hoàn tất việc chăm sóc (CareTaskForm) -> POST /api/groom/tasks/{id}/complete
 *   - Tích hợp bộ mô phỏng Live Payload Inspector hiển thị ngay JSON Request,
 *     HTTP Method và Endpoint Backend để đội Frontend và Giảng viên nghiệm thu.
 * ============================================================================
 */

export const Route = createFileRoute("/forms")({
  head: () => ({
    meta: [
      { title: "Khung Nhập Liệu (Groom & Club Manager) | Mã Phong" },
      {
        name: "description",
        content: "Trang nhập liệu chuẩn xác cho 2 vai trò Club Manager và Groom, kết nối Spring Boot Backend.",
      },
    ],
  }),
  component: FormsPage,
});

/**
 * Component chính của Trang /forms
 * Nhúng FormsNavigation chứa toàn bộ các biểu mẫu và thanh điều hướng theo Role
 */
function FormsPage() {
  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950 py-8 px-4 sm:px-6 lg:px-8">
      <FormsNavigation />
    </div>
  );
}
