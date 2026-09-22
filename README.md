# Đường Đua Số

Act as an expert Frontend Developer and UI/UX Designer. Create a responsive web dashboard for a "Racehorse Training & Management System" (Hệ thống Quản lý Huấn luyện Ngựa đua). 

TECH STACK: 

- React, JavaScript (JSX), Tailwind CSS.

- Use Lucide React for icons.

- Use Recharts for charts and graphs.

DESIGN SYSTEM & THEME:

- Primary Color: Saddle Brown (khoảng #8B4513) cho các nút bấm chính, thanh điều hướng, và các điểm nhấn.

- Secondary Color: Sienna (khoảng #A0522D) cho hover states, viền, hoặc các icon.

- Background/Text: Màu trắng (#FFFFFF) cho background chính, card background, kết hợp với chữ màu tối (Dark Gray/Black) để đảm bảo độ tương phản.

- Visual Elements: BẮT BUỘC chèn thêm các hình ảnh/vector về ngựa (có thể dùng Unsplash placeholders với keyword "racehorse", "stable") ở background của trang đăng nhập, banner header, hoặc avatar mặc định của ngựa để người dùng hình dung ngay bối cảnh hệ thống.

- Ngôn ngữ giao diện: 100% Tiếng Việt.

LAYOUT STRUCTURE:

- Sidebar (Menu trái): Màu nền Saddle Brown, chữ trắng. Bao gồm các menu chuyển đổi giữa các Role (để demo): [HLV Trưởng], [Bác sĩ thú y], [Nhân viên chuồng], [Chủ ngựa], [Quản lý CLB].

- Header: Thanh tìm kiếm, Icon thông báo (có chấm đỏ cảnh báo), Avatar người dùng.

- Main Content: Bố cục dạng Grid chứa các Card (bo góc, bóng đổ nhẹ).

YÊU CẦU CHI TIẾT CÁC TRANG & TÍNH NĂNG (Tích hợp 3 luồng Must-have):

1. Giao diện "HLV Trưởng" (Head Trainer View - Flow Huấn luyện):

- Bảng Dashboard: Hiển thị biểu đồ (Line chart) tiến độ và thể lực của các chiến mã.

- Lịch huấn luyện (Calendar/List): Card hiển thị lịch tập hôm nay, thông tin cự ly, mặt sân.

- Cảnh báo Real-time: Một panel hiển thị cảnh báo nhịp tim/vận tốc vượt ngưỡng (nháy màu đỏ).

- Modal đánh giá: Nút "Đánh giá phong độ" mở ra form nhập nhận xét và chấm điểm sau buổi tập.

2. Giao diện "Bác sĩ Thú y" (Veterinarian View - Flow Y tế & Chấn thương):

- Sơ đồ chuồng trại: Dạng lưới (Grid) hiển thị trạng thái từng con ngựa (Xanh: Khỏe, Vàng: Theo dõi, Đỏ: Chấn thương).

- Nút Action Khẩn cấp: Nút "KHÓA HUẤN LUYỆN" màu đỏ nổi bật (Red-600) dùng để vô hiệu hóa lịch tập của ngựa đang chấn thương.

- Hồ sơ y tế: Form cho phép chọn vị trí chấn thương trên danh sách/mô hình, cập nhật phác đồ điều trị và checklist tiêm phòng/tẩy giun.

3. Giao diện "Nhân viên Chăm sóc" (Groom View - Flow Vận hành):

- Todo List Hàng ngày: Danh sách checklist công việc (Cho ăn, Vệ sinh, Tắm rửa). Có checkbox để đánh dấu hoàn thành.

- Khẩu phần ăn: Card hiển thị chi tiết lượng ngũ cốc, cỏ, vitamin cho con ngựa đang chọn.

- Form Báo cáo sự cố: Form nhanh gồm Dropdown (Ngựa bỏ ăn, Sốt, Xước móng), ô text mô tả và nút "Upload hình ảnh".

4. Giao diện "Chủ Ngựa" (Horse Owner View - Flow Quản lý Lý lịch):

- Profile Ngựa (Pedigree): Một Card lớn thiết kế sang trọng, hiển thị hình ảnh chú ngựa đua, thông tin phả hệ, số tuổi, cân nặng.

- Lịch sử thi đấu: Bảng (Table) thống kê các giải đã đua, thứ hạng, và tiền thưởng.

- Trạng thái sức khỏe: Các thanh Progress bar hiển thị độ sẵn sàng, thể lực hiện tại (Read-only).

5. Giao diện "Quản lý Câu lạc bộ" (Club Manager View):

- Bảng điều khiển tài chính: Bar chart thể hiện chi phí vận hành (thức ăn, y tế) vs Doanh thu giải đấu.

- Bảng quản lý nhân sự và vật tư: Bảng danh sách với các nút phân quyền (RBAC) và nhật ký thao tác (Audit Log).

MỘT SỐ YÊU CẦU NÂNG CAO CHO UI:

- Thiết kế mượt mà, có animation khi hover vào các Card ngựa.

- Dữ liệu mock (giả lập) phải phong phú và mang đậm chất đua ngựa (VD: Tên ngựa: "Xích Thố", "Black Caviar"; Tên HLV: "Nguyễn Văn A").

- Trình bày toàn bộ trong một ứng dụng, dùng state để chuyển đổi giữa các View của từng Role.

Hãy generate bộ code hoàn chỉnh, đảm bảo giao diện đẹp, hiện đại, phối đúng tone màu Nâu - Trắng đã yêu cầu và mang lại cảm giác của một trường đua ngựa chuyên nghiệp.

This project was built with [Lovable](https://lovable.dev).

**Live app**: https://race-stable-hub.lovable.app

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/e3046025-06f4-4556-b1ef-e033707ee03c).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## Development

Prefer working locally? You need Node.js and npm — [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating).

```sh
git clone <this-repository-url>
cd <repository-name>
npm i
npm run dev
```
