# Hợp đồng dữ liệu API backend

Danh mục use case và URL nằm trong [USE_CASES.md](USE_CASES.md).
Tài liệu này mô tả dữ liệu API Auth, Admin và Groom trong `BE` của dự án này.
Nghiệp vụ Trainer, Vet và Owner không thuộc phạm vi triển khai.

## Đăng nhập và ánh xạ frontend

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"email":"manager@example.com","password":"mat_khau_cua_ban"}
```

Ví dụ kết quả 200 (token, ID, thời hạn và tập quyền là minh họa):

```json
{
  "accessToken": "chuoi_token_ngau_nhien",
  "tokenType": "Bearer",
  "expiresAt": "2026-09-26T12:00:00Z",
  "user": {
    "userId": 1,
    "fullName": "Quản lý câu lạc bộ",
    "email": "manager@example.com",
    "role": "Admin",
    "permissions": ["MANAGE_HORSES", "MANAGE_USERS"]
  }
}
```

`GET /api/auth/me` trả đúng cấu trúc của `user` ở trên. Token là chuỗi ngẫu nhiên,
không phải JWT; không decode token để suy ra role. `expiresAt` là thời điểm UTC.
Các API đăng ký/cập nhật từ chối trường JSON không được khai báo.

| Dữ liệu backend | Ánh xạ vào frontend hiện tại |
|---|---|
| `userId`: số | Có thể chuyển `String(userId)` cho trường `id` của UI; gửi ID số khi gọi API |
| `fullName` | `name` |
| `role: Admin` | `role: manager`, trang `/manager` |
| `role: Groom / Trainer / Vet / Owner` | `groom / trainer / vet / owner` |
| `permissions`: mảng mã quyền | Điều khiển nút giao diện; backend vẫn kiểm tra quyền |
| Chưa có `phone`, `avatarUrl`, `stableName` | Không gửi các trường này vào API hiện tại |

Đăng ký chỉ nhận `fullName`, `email`, `password`. Response 201:

```json
{"userId": 25, "status": "PENDING", "message": "Đăng ký thành công, chờ Admin duyệt."}
```

Không trả token khi đăng ký; Owner cần được duyệt trước khi đăng nhập.
Mật khẩu mới dài 10–72 ký tự và không vượt 72 byte UTF-8.

## Danh sách và dữ liệu tài nguyên

Ví dụ `GET /api/manager/users?role=Groom&status=ACTIVE&page=0&size=20`:

```json
{
  "items": [{"userId": 6, "fullName": "Nhân viên chuồng", "email": "groom@example.com", "roleId": 5, "role": "Groom", "status": "ACTIVE"}],
  "total": 1,
  "page": 0,
  "size": 20,
  "totalPages": 1
}
```

Danh sách rỗng trả `items: []`, `total: 0`, `totalPages: 0`. Trang ngoài kết quả
trả `items: []` nhưng `total` vẫn là tổng khớp bộ lọc. Bộ lọc `role`, `status`
phân biệt hoa/thường và nhận đúng giá trị đã công bố; giá trị sai trả 400.

| Tài nguyên | Các trường trả về |
|---|---|
| Ngựa | `horseId, name, gender, age, weight, lineage, ownerId, ownerName, archived, healthStatus` |
| Tài khoản Admin quản lý | `userId, fullName, email, roleId, role, status` |
| Role | `roleId, roleName` |
| Permission | `permissionId, permissionName` |
| Vật tư | `supplyId, itemName, type, quantityInStock, managedBy, managerName, version` |
| Chuồng/ngựa Groom phụ trách | `horseId, horseName, stableId, stallNumber, dailyRoutine` |
| Lịch Groom trong ngày | `scheduleId, horseId, horseName, date, time` |
| Khẩu phần | `feedId, horseId, grain, grass, vitamin, meal` |
| Công việc Groom | `taskId, horseId, horseName, taskType, completed, date` |
| Sự cố (danh sách, chi tiết, tạo mới) | `reportId, horseId, horseName, groomId, incidentType, description, date, imageUrl` |
| Nhật ký | `logId, userId, userName, action, targetTable, targetId, timestamp` |
| Khoản thu/chi | `entryId, horseId, entryType, amount, category, description, entryDate, createdBy` |
| Hiệu suất theo ngựa | `horseId, horseName, sessionCount, averageSpeed, averageHeartRate, careTaskCount, completedTaskCount` |
| Báo cáo tài chính | `income, expense, net, entryCount, currency, from, to, byCategory` |

ID là số; `completed`, `archived` là boolean. Các trường tùy chọn hoặc không có
thông tin trả `null` (ví dụ `lineage`, `healthStatus`, `imageUrl`). Số lượng và
số tiền trả JSON number; FE tự định dạng để hiển thị. `byCategory` là mảng
`{entryType, category, amount}`. Tạo khoản thu/chi trả 201 với `{entryId}`.

`date`/`entryDate` có dạng `2026-09-26`; `time` có dạng `08:00:00`.
`timestamp` của AuditLog là ngày giờ SQL Server không có múi giờ, ví dụ
`2026-09-26T08:30:00`; không tự diễn giải là UTC. Khi triển khai cần thống nhất
múi giờ máy chủ ứng dụng và SQL Server. Không đổi ngày lịch sang UTC ở frontend.

## Luồng Groom và ảnh sự cố

1. Đọc `/api/groom/stables?date=...` để chọn `horseId` được giao.
2. Đọc `/api/groom/tasks?date=...`; gửi POST `/{taskId}/complete` dưới `/api/groom/tasks`.
   Response trả đầy đủ công việc đã cập nhật; gọi lại không tạo thêm nhật ký hoàn thành.
3. Gửi POST `/api/groom/incidents` với `horseId, incidentType, description, date`.
4. Lấy `reportId` trong response rồi gửi POST `/api/groom/incidents/{id}/image`,
   multipart field `file`. Trình duyệt tự đặt Content-Type cùng boundary.
5. Upload thành công trả 204; GET lại chi tiết để nhận `imageUrl`.

`imageUrl` là đường dẫn API tương đối với base URL, ví dụ
`/api/groom/incidents/12/image`. Admin nhận đường dẫn tương ứng dưới `/api/manager`.
Ảnh chỉ đọc được với Bearer token; frontend tải bằng `fetch`, chuyển response
thành Blob rồi hiển thị bằng object URL. Không gắn trực tiếp đường dẫn này vào
`<img src>` vì thẻ img không tự gửi header Authorization.

Mỗi báo cáo hiện nhận một ảnh PNG/JPEG tối đa 5 MB, tối đa 16 triệu điểm ảnh.
Ảnh đọc về là PNG. `imageUrl: null` cũng áp dụng cho đường dẫn ảnh mẫu trong seed
không phải file đã upload qua backend. Tệp chưa tồn tại khi tải ảnh trả 404.

## Lỗi và quyền truy cập

Lỗi validation ví dụ:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Dữ liệu nhập không hợp lệ.",
  "instance": "/api/auth/login",
  "code": "INVALID_REQUEST",
  "errors": [{"field": "email", "message": "must be a well-formed email address"}]
}
```

Thông báo validation có thể phụ thuộc locale; frontend dùng `status`/`code` để
quyết định xử lý, không so sánh nguyên văn `detail`/`message`. `errors` là mảng
rỗng khi lỗi không gắn với trường body cụ thể.

| HTTP | `code` | Xử lý frontend |
|---|---|---|
| 400 | `INVALID_REQUEST` | Kiểm tra body, trường bắt buộc, query, enum và ngày |
| 401 | `UNAUTHENTICATED` | Đăng nhập lại khi token không hợp lệ/hết hạn |
| 403 | `FORBIDDEN` | Tài khoản chưa được duyệt/bị khóa khi login hoặc thiếu role/quyền |
| 404 | `NOT_FOUND` | Không tồn tại hoặc dữ liệu ngoài phạm vi được giao |
| 405 | `METHOD_NOT_ALLOWED` | Kiểm tra HTTP method |
| 406 | `NOT_ACCEPTABLE` | Kiểm tra header Accept |
| 409 | `CONFLICT` | Xử lý email trùng, chuyển trạng thái sai, tồn kho/version xung đột |
| 413 | `PAYLOAD_TOO_LARGE` | Giảm kích thước ảnh upload |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | Kiểm tra Content-Type |

Admin cần role `Admin` cùng các quyền: ngựa/sự cố `MANAGE_HORSES`; nhân sự
`MANAGE_USERS`; duyệt tài khoản `APPROVE_ACCOUNTS`; phân quyền `MANAGE_RBAC`;
vật tư `MANAGE_SUPPLIES`; báo cáo/đọc thu chi `VIEW_REPORTS`; tạo thu chi
`MANAGE_FINANCE`; nhật ký `VIEW_AUDIT_LOG`. Danh sách roles nhận `MANAGE_USERS`
hoặc `MANAGE_RBAC`. Groom cần role `Groom` cùng `VIEW_STABLE`, `VIEW_FEEDING`,
`COMPLETE_CARE`, `REPORT_INCIDENT`, `VIEW_SUPPLIES` theo nhóm API tương ứng.

Groom được cấp nhầm quyền Admin vẫn không vào nhóm `/api/manager`. Dữ liệu Groom
giới hạn theo tài khoản trong token; request không nhận `groomId` để chọn người khác.

Định dạng lỗi dựa trên [ProblemDetail của Spring Framework](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-ann-rest-exceptions.html).
