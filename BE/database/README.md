# Database quản lý ngựa — SQL Server

Nguồn: `../Horse_Management_ERD.drawio`, trang `ERD` (trang `Page-2` trống).
Database: **HorseManagement**. Có đủ **20 bảng**, dữ liệu mẫu trong tất cả các bảng,
khóa chính, khóa ngoại, chỉ mục và ràng buộc kiểm tra dữ liệu.

Mã nguồn Spring Boot nằm trong [BE](../); tài liệu ở [README dự án](../../README.md).
Khi dùng cho website, chạy thêm [migration backend](04_backend_extension.sql) sau bộ
SQL gốc. **Club Manager dùng role Admin**, theo yêu cầu nghiệp vụ. Migration
bổ sung trạng thái duyệt tài khoản, lưu trữ hồ sơ, token đăng nhập, dữ liệu thu/chi
và quyền truy cập API; xem hướng dẫn backend để cấu hình và chạy.

## Chức năng của từng file SQL

| File | Dùng để làm gì? | Khi nào chạy? |
|---|---|---|
| [00_create_database.sql](00_create_database.sql) | Tạo cơ sở dữ liệu `HorseManagement` nếu chưa tồn tại. | Chạy đầu tiên khi cài đặt database trên máy mới. |
| [01_schema.sql](01_schema.sql) | Tạo 20 bảng, khóa chính, 25 khóa ngoại, chỉ mục và các ràng buộc dữ liệu. | Chạy sau khi đã tạo database và chưa có bảng trong schema `dbo`. |
| [02_seed.sql](02_seed.sql) | Chèn 950 bản ghi mẫu vào các bảng để phát triển và thử nghiệm website. | Chạy sau khi tạo bảng; tất cả các bảng phải trống. |
| [03_verify.sql](03_verify.sql) | Kiểm tra bảng, số bản ghi và các ràng buộc sau khi nạp dữ liệu. | Chạy sau seed hoặc khi cần kiểm tra dữ liệu hiện tại. |
| [04_backend_extension.sql](04_backend_extension.sql) | Bổ sung trạng thái tài khoản, quyền API, token đăng nhập, thu/chi và các cột backend cần dùng. | Chạy sau bộ SQL gốc để sử dụng backend Spring Boot; có thể chạy lại. |
| [setup.sql](setup.sql) | Gọi lần lượt năm file `00`–`04` để cài dữ liệu gốc và backend. | Dùng khi cài mới bằng `sqlcmd` hoặc SSMS có bật SQLCMD Mode. |

### 00_create_database.sql — Tạo database

- Chuyển sang database hệ thống `master` để thực hiện lệnh tạo database.
- Kiểm tra tên `HorseManagement`: chưa có thì tạo, đã có thì bỏ qua.
- Chưa tạo bảng và chưa chèn dữ liệu; không xóa hoặc thay thế database đang có.

### 01_schema.sql — Tạo cấu trúc lưu trữ

- Tạo các bảng quản lý tài khoản và phân quyền, ngựa, tập luyện, chuồng trại,
  giải đua, y tế, chăm sóc, thức ăn, vật tư và nhật ký thao tác.
- Thiết lập khóa chính để xác định bản ghi và khóa ngoại để liên kết các bảng.
- Thiết lập các điều kiện như email duy nhất, cân nặng dương, số lượng tồn kho
  không âm và ngày kết thúc khóa tập không trước ngày bắt đầu.
- Tạo chỉ mục để hỗ trợ truy vấn theo các khóa ngoại và ngày tháng.
- Dừng nếu đã có bảng trong schema `dbo`. Các lệnh tạo cấu trúc được đặt trong
  một giao dịch để hoàn tác nếu có lỗi.

### 02_seed.sql — Chèn dữ liệu mẫu

- Tạo các vai trò `Admin`, `Owner`, `Trainer`, `Vet`, `Groom`, các quyền và
  liên kết cấp quyền cho từng vai trò.
- Thêm tổng cộng 25 người dùng, 25 ngựa, 10 giải đua và các hồ sơ liên quan;
  tổng số bản ghi của toàn bộ 20 bảng là 950.
- Chèn bảng cha trước bảng con để đáp ứng các khóa ngoại.
- Thực hiện trong một giao dịch; có lỗi thì hoàn tác toàn bộ dữ liệu vừa chèn.
- Yêu cầu tất cả bảng trống. Đây là file khởi tạo dữ liệu mẫu, không dùng để
  chạy lại mỗi lần mở website hay thêm dữ liệu nghiệp vụ hằng ngày.
- Các giá trị mật khẩu mẫu chưa dùng để đăng nhập; phần backend cần tạo và
  kiểm tra mã băm mật khẩu khi triển khai chức năng đăng nhập.

### 03_verify.sql — Kiểm tra sau cài đặt

- Kiểm tra đủ 20 bảng dự kiến và báo lỗi nếu có bảng bị thiếu hoặc chưa có dữ liệu.
- Hiển thị số dòng thực tế của từng bảng bên cạnh số dòng mẫu dự kiến.
- Kiểm tra khóa ngoại và ràng buộc `CHECK` có đang bật và được SQL Server xác nhận
  tính hợp lệ hay không.
- Chạy `DBCC CHECKCONSTRAINTS` để liệt kê vi phạm ràng buộc nếu có.
- Hiển thị tổng số khóa ngoại và tổng số bản ghi; sau khi cài mới đầy đủ,
  kết quả mong đợi là **25 khóa ngoại và 950 bản ghi**.
- Không sửa hoặc xóa dữ liệu nghiệp vụ. Có thể chạy lại để kiểm tra; số dòng
  dự kiến chỉ dùng đối chiếu, script không báo lỗi chỉ vì số dòng khác bộ mẫu.
  Khi website đã thêm/xóa dữ liệu, tổng số bản ghi có thể khác 950.

### 04_backend_extension.sql — Bổ sung cho backend website

- Club Manager dùng role `Admin` đã có; bổ sung các quyền API cho Admin và Groom.
- Thêm trạng thái duyệt tài khoản, cờ lưu trữ ngựa/vật tư, version vật tư và mô tả sự cố.
- Tạo bảng `ApiToken` phục vụ đăng nhập và `FinanceEntry` để lưu thu/chi.
- Chạy sau các file `00` đến `03`; không xóa dữ liệu cũ và có thể chạy lại.
- Sau extension có 22 bảng; số dòng không còn cố định ở 950 do có thêm quyền
  và dữ liệu hoạt động. Xem [hướng dẫn backend](../../README.md) để cấu hình.

### setup.sql — Cài database và phần bổ sung cho backend

File này không chứa định nghĩa bảng hay dữ liệu riêng. Nó dùng lệnh `:r`
để gọi năm file theo thứ tự, bao gồm `04_backend_extension.sql`:

```text
00_create_database.sql → 01_schema.sql → 02_seed.sql → 03_verify.sql → 04_backend_extension.sql
```

Lệnh `:On Error exit` yêu cầu dừng khi gặp lỗi. Chạy từ thư mục `database`
để các đường dẫn tương đối được tìm thấy. Chỉ chọn một cách cài đặt:
chạy `setup.sql` hoặc chạy từng file theo thứ tự, không chạy cả hai trên cùng
database đã có dữ liệu.

## Chạy bằng SQL Server Management Studio (SSMS)

1. Kết nối SQL Server, ví dụ server `localhost\SQLEXPRESS`, chọn Windows Authentication.
2. Mở và chạy toàn bộ `00_create_database.sql`.
3. Mở và chạy toàn bộ `01_schema.sql` để tạo bảng.
4. Chạy `02_seed.sql` để thêm toàn bộ 950 bản ghi mẫu trong một giao dịch.
5. Chạy `03_verify.sql` để kiểm tra số bản ghi và các ràng buộc dữ liệu.
6. Chạy `04_backend_extension.sql` để bổ sung cấu trúc và quyền cho backend.
7. Refresh **Databases → HorseManagement → Tables** trong Object Explorer.

Các file đánh số chạy được trong SSMS bình thường, không cần SQLCMD Mode.
Tài khoản cần quyền tạo database và tạo bảng/chèn dữ liệu trong database đó.

## Chạy một lần bằng PowerShell / sqlcmd

Đứng trong thư mục `database`, chạy:

```powershell
sqlcmd -S '.\SQLEXPRESS' -E -C -b -f 65001 -i setup.sql
```

`-E`: Windows Authentication; `-C`: tin cậy chứng chỉ của SQL Server cục bộ;
`-b`: dừng khi có lỗi; `-f 65001`: đọc file UTF-8, giữ đúng tiếng Việt.
`setup.sql` dùng lệnh `:r` nên chỉ chạy qua sqlcmd hoặc SSMS SQLCMD Mode
với thư mục làm việc là thư mục này.

Script tạo database nếu chưa tồn tại, **không DROP database/bảng**.
Script schema sẽ dừng nếu database đã có bảng `dbo`, tránh thay đổi dữ liệu sẵn có.
Script seed yêu cầu các bảng trống và dùng transaction; chạy lại sau khi đã seed
sẽ báo lỗi, không tạo bản ghi trùng. Không chạy lại `setup.sql` để xem dữ liệu;
chỉ chạy `03_verify.sql` hoặc truy vấn SELECT trên các bảng cần xem.

## Ánh xạ và điều chỉnh so với ERD

| Trong ERD | Trong SQL | Lý do |
|---|---|---|
| TRAININGSCHDULE | TRAININGSCHEDULE | Sửa lỗi chính tả |
| TRAININGSESSION.SesstionID | TRAININGSESSION.SessionID | Sửa lỗi chính tả |
| TRAININGSCHDULE.Groom | TRAININGSCHEDULE.GroomID | Thống nhất tên khóa ngoại |
| PERMISSION.RoleName | PERMISSION.PermissionName | Tên quyền thay vì tên vai trò |
| demical | DECIMAL(p,s) | Kiểu dữ liệu hợp lệ, có độ chính xác cụ thể |
| varchar / text | NVARCHAR / NVARCHAR(MAX) | Lưu được tiếng Việt; Email và Password dùng VARCHAR |
| boolean | BIT | Kiểu SQL Server; CARETASK.Status: 0 chưa xong, 1 đã xong |
| AuditLog.TimeStamp: dattime | DATETIME2(0) | Lưu ngày giờ, không dùng kiểu timestamp/rowversion |
| ROLEPERMISSION chỉ có hai FK | PK ghép (RoleID, PermissionID) | Không cấp trùng một quyền cho một vai trò |
| HEALTHSTATUS.HorseID chỉ ghi PK | Đồng thời là FK tới Horse | Mỗi ngựa có tối đa một trạng thái hiện tại |
| Horse.OwnerID, TRAININGPLAN.CreatedBy | FK tới USER.UserID | Theo ý nghĩa các thuộc tính đã ghi trong ERD |
| TRAININGLOCK.VetID, CARETASK.GroomID, AuditLog.UserID | FK tới USER.UserID | Hoàn thiện các liên kết người dùng |

Các khóa chính INT đơn dùng `IDENTITY(1,1)`, ngoại trừ HEALTHSTATUS.HorseID
được lấy từ Horse. RACEREGISTRATION giữ PK ghép (HorseID, RaceID) như ERD.
Tên `USER`, `ROLE` và các cột như `Date`, `Time`, `Password`, `Comment`
được đặt trong dấu ngoặc vuông khi viết SQL.

## Các bảng và số dòng mẫu (sau khi chạy 02_seed.sql)

| Bảng | Số dòng |
|---|---:|
| ROLE | 5 |
| PERMISSION | 9 |
| USER | 25 |
| ROLEPERMISSION | 18 |
| Horse | 25 |
| TRAININGPLAN | 45 |
| TRAININGSCHEDULE | 165 |
| TRAININGSESSION | 165 |
| STABLEASSIGNMENT | 25 |
| RACE | 10 |
| RACEREGISTRATION | 66 |
| MEDICALRECORD | 45 |
| TRAININGLOCK | 8 |
| CARETASK | 145 |
| HEALTHSTATUS | 25 |
| VACCINATIONSCHEDULE | 45 |
| FEEDINGPLAN | 66 |
| Supply | 20 |
| AuditLog | 25 |
| INCIDENTREPORT | 13 |

Dữ liệu hoàn toàn giả lập. Tài khoản mẫu dùng địa chỉ `example.com`.
`USER.Password` chứa dấu hiệu tài khoản demo không đăng nhập được, không phải
mật khẩu thật hay hash dùng được. Khi tích hợp đăng nhập, ứng dụng cần tạo và kiểm tra
hash bằng thư viện mật khẩu phù hợp; không so sánh mật khẩu dạng plaintext.
ImagePath để NULL vì chưa có ảnh đính kèm thực tế.

## Quy ước và giới hạn nghiệp vụ

- Trường bắt buộc dùng NOT NULL. Lineage, Comment, DailyRoutine, Treatment,
  InjuryLocation và ImagePath cho phép NULL; TRAININGLOCK.EndDate = NULL
  nghĩa là chưa xác định ngày kết thúc khóa tập.
- Email, tên vai trò và tên quyền là duy nhất. Tuổi, quãng đường, workload,
  tốc độ, định lượng thức ăn và tồn kho không âm; cân nặng và nhịp tim phải dương.
- Dữ liệu mẫu quy ước Weight tính bằng kg, Distance bằng km, Speed bằng km/h,
  HeartRate bằng nhịp/phút, Workload bằng phút, Grain/Grass/Vitamin bằng kg/bữa.
  ERD chưa xác định đơn vị; có thể đổi quy ước đồng bộ ở ứng dụng.
- Không tự xóa dây chuyền: phải xử lý dữ liệu phụ thuộc trước khi xóa bản ghi cha.
- FK người dùng đảm bảo UserID tồn tại; việc OwnerID phải có vai trò Owner,
  VetID phải có vai trò Vet, hoặc GroomID phải có vai trò Groom do tầng nghiệp vụ kiểm tra.
- ERD chưa liên kết TrainingPlan, TrainingSchedule và TrainingSession với nhau,
  nên SQL giữ nguyên quan hệ riêng của từng bảng với Horse.
- STABLEASSIGNMENT giữ quan hệ nhiều bản ghi với một Horse theo ERD;
  chưa thêm quy tắc một ngựa/một chuồng hoặc lịch sử chuyển chuồng.
- Kiểm tra trùng lịch, chặn tập khi đang TRAININGLOCK, ngày đăng ký trước ngày đua
  và phân quyền theo chủ ngựa cần được thực hiện ở tầng nghiệp vụ.
- AuditLog không tự sinh bằng trigger. Ứng dụng ghi log; TargetTable/TargetID là
  tham chiếu linh hoạt nên không có FK tới từng bảng. ERD chỉ có TargetID INT,
  chưa biểu diễn đủ khóa ghép của RACEREGISTRATION và ROLEPERMISSION.
- Các giá trị Phase, Gender, trạng thái dạng chữ chưa bị giới hạn thành danh sách
  cố định vì ERD chưa mô tả tập giá trị hợp lệ.

## Thêm dữ liệu mới

Xem các câu INSERT đầy đủ cột trong `02_seed.sql` cho cả 20 thực thể.
Khi thêm dữ liệu thường xuyên, **bỏ cột ID tự tăng**, không cần IDENTITY_INSERT;
lấy ID mới bằng `SCOPE_IDENTITY()` hoặc `OUTPUT INSERTED.<ID>`.
Chèn bản ghi cha trước (ROLE, PERMISSION, USER, Horse, RACE), rồi chèn các
bảng phụ thuộc. Khi thêm nhiều bản ghi liên quan, dùng cùng một transaction
để có thể hoàn tác toàn bộ nếu có lỗi.

## File seed đã gộp

`02_seed.sql` chứa toàn bộ **950 bản ghi** (115 bản ghi ban đầu và 835 bản ghi
mở rộng) trong cùng một giao dịch. Nếu một câu lệnh lỗi, toàn bộ dữ liệu chèn
trong lần chạy được hoàn tác. File yêu cầu các bảng trống, không dùng để chạy
lại trên database đã có dữ liệu; dữ liệu hiện tại vẫn được giữ nguyên.

`setup.sql` chạy lần lượt các file tạo database, tạo bảng, seed và kiểm tra
dữ liệu. Các chú thích SQL đều được viết bằng tiếng Việt.
Giữ nguyên 5 vai trò, 9 quyền, 18 liên kết phân quyền và toàn bộ dữ liệu mẫu.
