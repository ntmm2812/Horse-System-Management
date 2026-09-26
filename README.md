# Horse Management — Club Manager & Groom

Dự án backend độc lập cho **Club Manager (role `Admin`)** và **Groom** theo
[USECASE.drawio](USECASE.drawio). Không có frontend trong thư mục này.

Tái sử dụng backend, SQL và bộ kiểm thử phù hợp từ workspace hiện có.
Repository Git riêng được khởi tạo với nhánh mặc định `main`; chưa tạo commit
hoặc liên kết remote. Bản clone GitHub và các thư mục nguồn bên ngoài không bị sửa.

## Công nghệ và cấu trúc

Java 17+, Spring Boot 3.5.16, Spring Security, Spring JDBC, SQL Server.
Kiểm thử tích hợp dùng MockMvc và H2 ở chế độ tương thích SQL Server.

```text
Horse-Management-Project/
├── BE/
│   ├── pom.xml, mvnw, mvnw.cmd, .mvn/
│   ├── database/                SQL, 950 bản ghi mẫu và ERD
│   └── src/
│       ├── main/java/com/horsemanagement/
│       │   ├── HorseManagementApplication.java
│       │   ├── config/          Security và bootstrap Admin
│       │   ├── controller/      API Auth, Manager, Groom, báo cáo, ảnh sự cố
│       │   ├── dto/             Request validation và response xác thực
│       │   ├── exception/       Lỗi application/problem+json
│       │   ├── repository/      JDBC truy vấn có tham số
│       │   ├── security/        Bearer token, role và permission
│       │   └── service/         Nghiệp vụ và nhật ký thao tác
│       ├── main/resources/application.yml
│       └── test/                Kiểm thử API và dữ liệu kiểm thử riêng
├── docs/
│   ├── USE_CASES.md             Đối chiếu use case → API → kiểm thử
│   ├── api-contract.md          Body, response, phân trang, lỗi
│   └── api.http                 Request mẫu để thử API
└── USECASE.drawio
```

## Use case đã có

**Club Manager:** quản lý hồ sơ ngựa, quản lý nhân sự, quản lý vật tư,
phân quyền RBAC, xem báo cáo hiệu suất/tài chính, theo dõi nhật ký thao tác,
duyệt tài khoản.

**Groom:** xem chuồng trại và lịch sinh hoạt, xem khẩu phần ăn, xác nhận hoàn
thành việc, báo cáo sự cố kèm ảnh, theo dõi vật tư.

**Xác thực hỗ trợ:** đăng nhập, xem hồ sơ, đăng xuất, đổi mật khẩu; đăng ký Owner
chờ duyệt để phục vụ luồng duyệt tài khoản. Các API nghiệp vụ Trainer, Vet và
Owner không thuộc phạm vi này. Xem [danh mục chi tiết](docs/USE_CASES.md).

## Cài database

Cần SQL Server và JDK 17 trở lên. Trong `BE/database`, với database mới:

```powershell
sqlcmd -S '.\SQLEXPRESS' -E -C -b -f 65001 -i setup.sql
```

Thay instance theo máy. Hoặc chạy từng file `00` → `01` → `02` → `03` → `04`
trong SSMS. `setup.sql` đã bao gồm extension `04_backend_extension.sql`.
Nếu database đã có schema và seed gốc, chỉ chạy `04_backend_extension.sql`.
Script schema/seed yêu cầu database chưa có bảng/dữ liệu tương ứng.

Bộ gốc có 20 bảng và 950 bản ghi. Extension bổ sung `ApiToken`, `FinanceEntry`,
trạng thái tài khoản, cờ lưu trữ, phiên bản vật tư, mô tả sự cố và các quyền API.
Số bản ghi sau extension/bootstrap sẽ khác 950. Báo cáo tài chính dùng các khoản
thu/chi được nhập qua API, không tự tạo doanh thu giả từ dữ liệu tập luyện.

## Chạy backend

Mở PowerShell trong `BE`, đặt `JAVA_HOME` trỏ đến JDK 17+ rồi cấu hình:

```powershell
$env:DB_URL = 'jdbc:sqlserver://localhost:1433;databaseName=HorseManagement;encrypt=true;trustServerCertificate=true'
$env:DB_USERNAME = 'TEN_TAI_KHOAN_SQL'
$env:DB_PASSWORD = 'MAT_KHAU_SQL'
$env:BOOTSTRAP_ENABLED = 'true'
$env:BOOTSTRAP_EMAIL = 'manager@example.com'
$env:BOOTSTRAP_PASSWORD = 'THAY_BANG_MAT_KHAU_RIENG_TOI_THIEU_12_KY_TU'
.\mvnw.cmd spring-boot:run
```

Thay các giá trị mẫu bằng cấu hình thật. `trustServerCertificate=true` dành cho
SQL Server cục bộ với chứng chỉ tự ký. Spring Boot đọc biến môi trường, không tự
nạp `.env`. Maven Wrapper cần mạng ở lần tải Maven/dependency đầu tiên.

Bootstrap tạo Admin đầu tiên với email chưa có trong seed. Sau khi tạo, đặt
`BOOTSTRAP_ENABLED=false` và bỏ biến mật khẩu bootstrap. Mật khẩu seed là
placeholder; dùng Admin để đặt lại mật khẩu Groom mẫu hoặc tạo nhân sự mới.

API: `http://localhost:8080`. Đăng nhập qua `POST /api/auth/login`, sau đó gửi
`Authorization: Bearer <accessToken>`. Token ngẫu nhiên được lưu dưới dạng hash
và có thể thu hồi; đây không phải JWT. Role và quyền được kiểm tra mỗi request.
Xem [request mẫu](docs/api.http) và [hợp đồng API](docs/api-contract.md).

## Kiểm thử và đóng gói

Trong `BE`:

```powershell
.\mvnw.cmd clean verify
```

Kiểm thử dùng database H2 riêng, không cần tài khoản SQL Server. Kết quả ở
`BE/target/surefire-reports`; JAR ở `BE/target/horse-club-be-0.0.1-SNAPSHOT.jar`.
Chạy JAR từ `BE` với các biến môi trường trên:

```powershell
java -jar target/horse-club-be-0.0.1-SNAPSHOT.jar
```

Ảnh sự cố được lưu ở `storage/incidents` của dự án nếu chạy từ `BE`.
Có thể chỉ định đường dẫn tuyệt đối bằng `UPLOAD_DIR`.

## Quy tắc nghiệp vụ

- Club Manager dùng role `Admin`, URL `/api/manager`; Groom dùng `/api/groom`.
- Groom chỉ thấy ngựa được giao qua `CARETASK` hoặc `TRAININGSCHEDULE` trong
  ngày yêu cầu; chỉ hoàn thành công việc của mình, không hoàn thành việc tương lai.
- Gọi hoàn thành nhiều lần không tạo thêm nhật ký hoàn thành.
- Ngựa/vật tư được lưu trữ thay vì xóa lịch sử. Nhập/xuất kho kiểm tra phiên bản
  và không cho tồn kho âm.
- Đổi mật khẩu, khóa tài khoản hoặc đổi role thu hồi token liên quan.
- Ảnh sự cố yêu cầu xác thực, PNG/JPEG tối đa 5 MB và 16 triệu pixel.
- Phân công việc chuồng trại, lập lịch tập và lập khẩu phần thuộc Head Trainer
  trong sơ đồ; Groom đọc các dữ liệu đã được phân công.

Kiểm thử H2 không thay thế kiểm chứng trên SQL Server thật. Cần cấu hình SQL
Server của máy trước khi chạy ứng dụng với bộ dữ liệu mẫu.
