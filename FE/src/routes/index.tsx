import { createFileRoute, Link } from "@tanstack/react-router";
import { FormsNavigation } from "@/components/forms/FormsNavigation";

/**
 * ============================================================================
 * FILE: index.tsx
 * ĐƯỜNG DẪN TRUY CẬP: / (Trang chủ hệ thống)
 * MỤC ĐÍCH:
 *   - Trang chủ hiển thị trực tiếp Khung Nhập Liệu (Input Forms) theo 2 vai trò
 *     trọng tâm của dự án:
 *       1. Club Manager (Quản lý CLB)
 *       2. Groom (Nhân viên Chăm sóc Ngựa)
 *   - Giúp người dùng, đội Frontend và Giảng viên có thể xem, tương tác, kiểm tra
 *     và lấy khung cấu trúc dữ liệu ngay lập tức mà không cần qua các bước đăng nhập
 *     hoặc chuyển hướng phức tạp.
 *   - Kết nối trực tiếp với các API Spring Boot của com.horsemanagement.controller.
 * ============================================================================
 */

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Cổng Khung Nhập Liệu — Groom & Club Manager | Mã Phong" },
      {
        name: "description",
        content: "Trang nhập liệu trung tâm cho 2 vai trò Groom và Club Manager, kết nối Spring Boot Backend.",
      },
    ],
  }),
  component: IndexPage,
});

/**
 * Component Trang chủ IndexPage
 * Trực tiếp render Khung Nhập Liệu theo vai trò Groom & Club Manager
 */
function IndexPage() {
  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950">
      {/* Thanh Header đầu trang */}
      <header className="border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 sticky top-0 z-30">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-2xl">🐎</span>
            <div>
              <span className="font-bold text-zinc-900 dark:text-zinc-100 text-base">Mã Phong — SWP391</span>
              <span className="hidden sm:inline-block ml-2 text-xs px-2 py-0.5 rounded bg-amber-100 dark:bg-amber-950/60 text-amber-800 dark:text-amber-300 font-medium">
                Khung Nhập Liệu Groom & Club Manager
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3 text-xs">
            <Link
              to="/manager"
              className="text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100"
            >
              Dashboard Manager
            </Link>
            <span className="text-zinc-300 dark:text-zinc-700">|</span>
            <Link
              to="/groom"
              className="text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100"
            >
              Dashboard Groom
            </Link>
            <span className="text-zinc-300 dark:text-zinc-700">|</span>
            <Link
              to="/login"
              className="text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100"
            >
              Đăng nhập
            </Link>
          </div>
        </div>
      </header>

      {/* Vùng hiển thị Nội dung Khung Nhập Liệu */}
      <main className="py-6">
        <FormsNavigation />
      </main>
    </div>
  );
}
