# Use case đã triển khai — Club Manager và Groom

Đối chiếu trực tiếp nhãn trong [USECASE.drawio](../USECASE.drawio).
Các ID bên dưới lấy hậu tố của cell `-bmsM7qnjG3FFlsMT_DN-…` để truy vết.
Club Manager ánh xạ role **Admin** trong SQL, Groom giữ role **Groom**.

## Club Manager — 7 use case

| ID sơ đồ | Use case | API đã triển khai | Hành vi |
|---|---|---|---|
| 46 | Quản lý hồ sơ ngựa | `GET/POST /api/manager/horses`; `GET/PUT/DELETE /api/manager/horses/{id}` | Tìm kiếm, phân trang, xem chi tiết, thêm, sửa, lưu trữ; kiểm tra chủ ngựa đang hoạt động và giữ lịch sử |
| 47 | Quản lý nhân sự | `GET/POST /api/manager/users`; `GET/PUT /api/manager/users/{id}`; `PATCH /api/manager/users/{id}/status`; `PUT /api/manager/users/{id}/password` | Tạo tài khoản nhân sự, lọc theo role/trạng thái, cập nhật, khóa/mở khóa, đặt lại mật khẩu; thu hồi token khi cần |
| 48 | Quản lý vật tư | `GET/POST /api/manager/supplies`; `GET/PUT/DELETE /api/manager/supplies/{id}`; `POST /api/manager/supplies/{id}/adjustments` | Quản lý danh mục, nhập/xuất kho có lý do, lưu trữ; không cho âm kho và từ chối phiên bản cũ |
| 49 | Phân quyền (RBAC) | `GET /api/manager/roles`; `GET /api/manager/permissions`; `GET/PUT /api/manager/roles/{id}/permissions` | Xem và thay tập quyền role; có hiệu lực ngay; giữ quyền quản trị cốt lõi |
| 50 | Xem báo cáo hiệu suất/tài chính | `GET /api/manager/reports/performance`; `GET /api/manager/reports/finance` | Lọc `from`, `to`; tổng hợp buổi tập, tốc độ, nhịp tim, việc chăm sóc; thu/chi, chênh lệch và nhóm khoản mục |
| 51 | Theo dõi nhật ký thao tác | `GET /api/manager/audit-logs` | Phân trang, lọc `userId`, `table`; xem người thao tác, hành động, đối tượng và thời gian |
| 52 | Duyệt tài khoản | `POST /api/manager/users/{id}/approval` | `approved=true/false` để duyệt/từ chối tài khoản PENDING; không duyệt lại tài khoản đã xử lý |

Code chính: [ManagerController](../BE/src/main/java/com/horsemanagement/controller/ManagerController.java),
[ManagerService](../BE/src/main/java/com/horsemanagement/service/ManagerService.java),
[ReportController](../BE/src/main/java/com/horsemanagement/controller/ReportController.java).

API hỗ trợ báo cáo tài chính: `GET/POST /api/manager/finance-entries`.
Tạo khoản thu/chi yêu cầu `MANAGE_FINANCE`; báo cáo yêu cầu `VIEW_REPORTS`.
Bảng `FinanceEntry` bổ sung nguồn dữ liệu thu/chi vì ERD gốc chưa có bảng tài chính.

## Groom / Stable Hand — 5 use case

| ID sơ đồ | Use case | API đã triển khai | Hành vi |
|---|---|---|---|
| 39 | Xem chuồng trại & lịch sinh hoạt | `GET /api/groom/stables`; `GET /api/groom/schedule`; `GET /api/groom/tasks` | Xem chuồng, ô chuồng, sinh hoạt, lịch tập và việc chăm sóc theo ngày được phân công |
| 40 | Xem khẩu phần ăn | `GET /api/groom/horses/{id}/feeding-plan` | Xem bữa ăn, ngũ cốc, cỏ, vitamin của ngựa được giao trong ngày |
| 41 | Xác nhận hoàn thành việc | `POST /api/groom/tasks/{id}/complete` | Chỉ cập nhật việc của Groom đăng nhập, không cho hoàn thành trước ngày; gọi lặp không ghi thêm log |
| 44 | Báo cáo sự cố | `GET/POST /api/groom/incidents`; `GET /api/groom/incidents/{id}`; `POST/GET /api/groom/incidents/{id}/image` | Tạo/xem báo cáo của mình cho ngựa được giao, mô tả và ngày; đính kèm ảnh PNG/JPEG có xác thực |
| 45 | Theo dõi vật tư | `GET /api/groom/supplies` | Xem, tìm kiếm và phân trang vật tư đang hoạt động; Groom không được sửa kho |

Code chính: [GroomController](../BE/src/main/java/com/horsemanagement/controller/GroomController.java),
[GroomService](../BE/src/main/java/com/horsemanagement/service/GroomService.java),
[IncidentImageService](../BE/src/main/java/com/horsemanagement/service/IncidentImageService.java).

Manager xem báo cáo sự cố qua `GET /api/manager/incidents`,
`GET /api/manager/incidents/{id}` và `GET /api/manager/incidents/{id}/image`.

ERD chưa có bảng phân công Groom–Horse riêng; quyền đọc được xác định bằng
`CARETASK` hoặc `TRAININGSCHEDULE` cùng ngựa, Groom và ngày yêu cầu.
Nếu không truyền `date`, hệ thống dùng ngày hiện tại của máy chủ.
Lập lịch tập, phân công việc và lập khẩu phần thuộc **Head Trainer** trong sơ đồ,
không triển khai quyền sửa các dữ liệu đó cho Groom.

## Đăng nhập và chức năng hỗ trợ

Use case chung `Đăng nhập` là cell `y5ALJKPVJW64wi63CnY0-1`.

| API | Mục đích |
|---|---|
| `POST /api/auth/login` | Đăng nhập email/password; trả Bearer token và hồ sơ có role/quyền |
| `GET /api/auth/me` | Hồ sơ của người đang đăng nhập |
| `POST /api/auth/logout` | Thu hồi token hiện tại |
| `PUT /api/auth/password` | Đổi mật khẩu và thu hồi các token của người đó |
| `POST /api/auth/register` | Chỉ tạo Owner PENDING, phục vụ luồng Manager duyệt tài khoản; không cho tự chọn role đặc quyền |

Mật khẩu băm BCrypt; chỉ hash token được lưu trong database. API kiểm tra cả
role, permission và quyền đối với dữ liệu liên quan. Các lỗi trả
`application/problem+json`; có kiểm tra đầu vào và giới hạn phân trang.

## Kiểm thử theo use case

File: [ApiIntegrationTest.java](../BE/src/test/java/com/horsemanagement/ApiIntegrationTest.java).
Chạy `./mvnw.cmd verify` từ `BE` trên Windows.

| Phạm vi | Test tiêu biểu |
|---|---|
| Xác thực, cách ly role, logout | `rolesTokensAndLogoutAreEnforced` |
| Duyệt/từ chối và chống tự cấp quyền | `registrationNeedsApprovalAndCannotChoosePrivilegedRole`, `rejectedRegistrationCannotLoginOrBeApprovedAgain` |
| Nhân sự, băm mật khẩu, khóa tài khoản | `employeeManagementHashesPasswordsRevokesAccessAndProtectsAdmin` |
| RBAC có hiệu lực ngay | `permissionChangesApplyImmediatelyAndDoNotCrossRoleBoundary` |
| Hồ sơ ngựa và giữ lịch sử | `horseValidationAndArchivePreserveHistory`, `managerUpdatesHorseArchivesSupplyAndCanFilterAuditTrail` |
| Tồn kho, phiên bản và audit | `inventoryRejectsNegativeStockAndStaleWrites`, `managerUpdatesHorseArchivesSupplyAndCanFilterAuditTrail` |
| Chuồng, lịch, khẩu phần và giới hạn Groom | `groomReadsOnlyAssignedDayAndCannotWriteInventory` |
| Hoàn thành việc đúng người/ngày | `groomOnlySeesAssignedHorsesAndCompletesOwnPastOrCurrentTasks` |
| Sự cố, ảnh và quyền sở hữu | `incidentAndAttachmentEnforceAssignmentOwnershipAndImageFormat` |
| Báo cáo có dữ liệu thật và khoảng ngày | `financeAndPerformanceReportsUseStoredDataAndValidateDates` |
| Mật khẩu và token hết hạn | `passwordChangeAndExpiredTokensCannotKeepAccess` |
| Hợp đồng login, bộ lọc, ngày giờ và lỗi | `loginProfileMatchesMeAndDoesNotAcceptClientChosenRole`, `paginatedUserFiltersAndTemporalFieldsMatchTheContract`, `securityAndMvcErrorsShareProblemDetails` |

Các test tích hợp dùng H2, không thực thi bộ SQL Server `00`–`04`.
Khởi tạo SQL Server và kiểm chứng bộ dữ liệu mẫu cần môi trường SQL Server thật.

Kết quả xác minh ngày 26/09/2026: Maven `verify` **BUILD SUCCESS**, 16 test,
0 thất bại, 0 lỗi, 0 bỏ qua. Chạy bằng JDK 25.0.2 và Maven 3.9.11 với dependency
cache cục bộ; mã nguồn được biên dịch với Java release 17 theo `pom.xml`.
Đã đóng gói JAR Spring Boot tại `BE/target/horse-club-be-0.0.1-SNAPSHOT.jar`.
Chưa chạy bộ SQL `00`–`04` trên SQL Server thật trong lần thực hiện này.
