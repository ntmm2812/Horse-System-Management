# Backend

Hướng dẫn cài SQL Server, bootstrap Admin, chạy và kiểm thử tại
[README dự án](../README.md).

- [Các use case và API](../docs/USE_CASES.md)
- [Hợp đồng request/response](../docs/api-contract.md)
- [Request mẫu](../docs/api.http)
- [SQL và dữ liệu mẫu](database/README.md)

Mở thư mục này như Maven project. Java 17+; lớp khởi động là
`com.horsemanagement.HorseManagementApplication`.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Chạy ứng dụng cần cấu hình SQL Server theo README; kiểm thử dùng H2 riêng.
