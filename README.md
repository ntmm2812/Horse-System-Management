# Mã Phong — Race Stable Hub
### Hệ thống Quản lý Huấn luyện & Chăm sóc Ngựa đua Chuyên nghiệp

Hệ thống quản lý toàn diện dành cho trường đua và câu lạc bộ ngựa đua, cung cấp không gian làm việc chuyên biệt cho từng vai trò: **Huấn luyện viên trưởng**, **Bác sĩ thú y**, **Nhân viên chuồng**, **Chủ ngựa** và **Ban quản lý**.

---

## 🌟 Tính năng Nổi bật

### 1. 🏇 Huấn luyện viên trưởng (Head Trainer)
- **Bảng Dashboard phong độ**: Theo dõi chỉ số thể lực, tiến độ qua từng ngày tập luyện với biểu đồ trực quan.
- **Lịch tập luyện chuyên sâu**: Quản lý cự ly, cường độ và tình trạng mặt sân (cỏ, cát).
- **Cảnh báo Real-time**: Giám sát nhịp tim và vận tốc vượt ngưỡng an toàn.
- **Đánh giá phong độ**: Chấm điểm và ghi chú chi tiết sau mỗi buổi tập.

### 2. 🩺 Bác sĩ Thú y (Veterinarian)
- **Sơ đồ chuồng trại**: Trực quan hóa trạng thái sức khỏe từng chiến mã (Khỏe mạnh, Cần theo dõi, Chấn thương).
- **Khóa huấn luyện khẩn cấp**: Vô hiệu hóa lịch tập ngay lập tức đối với ngựa gặp chấn thương.
- **Hồ sơ bệnh án & Tiêm chủng**: Ghi nhận phác đồ điều trị, lịch tiêm phòng và tẩy giun định kỳ.

### 3. 🧹 Nhân viên Chuồng (Groom / Caretaker)
- **Checklist công việc hằng ngày**: Quản lý lịch cho ăn, tắm rửa, vệ sinh chuồng trại.
- **Khẩu phần dinh dưỡng**: Theo dõi định lượng cỏ khô, cám yến mạch, khoáng chất và điện giải.
- **Báo cáo sự cố nhanh**: Gửi phản ánh tức thời khi ngựa có biểu hiện bất thường.

### 4. 🏆 Chủ Ngựa (Horse Owner)
- **Hồ sơ & Phả hệ (Pedigree)**: Thông tin chi tiết về xuất xứ, dòng giống, thành tích.
- **Lịch sử thi đấu & Tiền thưởng**: Thống kê kết quả các cúp đua, thứ hạng và giải thưởng.
- **Chỉ số sẵn sàng**: Theo dõi độ bền, tốc độ và tâm lý ngựa đua trước giải đấu.

### 5. 📊 Quản lý Câu lạc bộ (Club Management)
- **Báo cáo tài chính**: Đối soát chi phí vận hành (dinh dưỡng, y tế, bảo trì) với doanh thu thi đấu.
- **Phân quyền & Kiểm toán (Audit Logs)**: Nhật ký hoạt động chi tiết của toàn bộ nhân sự.

---

## Trang theo vai trò

| Vai trò | Đường dẫn | Nội dung chính |
| --- | --- | --- |
| HLV Trưởng | `/trainer` | Thể lực, cảnh báo và lịch huấn luyện |
| Bác sĩ thú y | `/vet` | Sơ đồ chuồng và hồ sơ y tế |
| Nhân viên chuồng | `/groom` | Công việc ca trực, dinh dưỡng và báo cáo sự cố |
| Chủ ngựa | `/owner` | Hồ sơ, phả hệ, thành tích và sức khỏe |
| Quản lý CLB | `/manager` | Tài chính, nhân sự, vật tư và nhật ký |

Mỗi trang có menu nghiệp vụ riêng. `/` đưa người dùng tới trang tương ứng với vai trò đã đăng nhập; khách chưa đăng nhập được chuyển tới `/login`. Mở URL của vai trò khác sẽ chuyển về trang của tài khoản hiện tại. Muốn thử vai trò khác, đăng xuất ở menu tài khoản rồi chọn tài khoản mẫu tại `/login`.

Đăng nhập hiện là bản demo lưu phiên trong `sessionStorage`; việc giới hạn trang nằm ở giao diện, chưa thay thế xác thực và phân quyền dữ liệu phía máy chủ/Supabase RLS.

---

## 🛠 Tech Stack

- **Frontend**: [React 19](https://react.dev/), [TanStack Start](https://tanstack.com/start), [TanStack Router](https://tanstack.com/router)
- **Styling**: [Tailwind CSS v4](https://tailwindcss.com/), [Lucide React Icons](https://lucide.dev/)
- **Charts**: [Recharts](https://recharts.org/)
- **State & Data**: [TanStack Query](https://tanstack.com/query)
- **Backend & Database**: [Supabase](https://supabase.com/)
- **Bundler**: [Vite 8](https://vitejs.dev/)

---

## 🚀 Cài đặt & Chạy ứng dụng

1. **Cài đặt dependencies**:
   ```bash
   npm install
   ```

2. **Cấu hình biến môi trường**:
   Tạo file `.env` từ `.env.example` (nếu cần kết nối Supabase):
   ```env
   VITE_SUPABASE_URL=your_supabase_url
   VITE_SUPABASE_PUBLISHABLE_KEY=your_supabase_publishable_key
   ```

3. **Chạy máy chủ phát triển**:
   ```bash
   npm run dev
   ```

4. **Build dự án**:
   ```bash
   npm run build
   ```
