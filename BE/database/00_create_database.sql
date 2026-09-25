-- Chạy trên SQL Server 2016 trở lên bằng tài khoản có quyền tạo cơ sở dữ liệu.
USE [master];
GO
IF DB_ID(N'HorseManagement') IS NULL
    EXEC(N'CREATE DATABASE [HorseManagement];');
GO
