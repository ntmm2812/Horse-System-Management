-- Bổ sung dữ liệu cần cho backend; chạy sau các file 00, 01, 02, 03 cùng thư mục.
-- Club Manager dùng role Admin đã có. Không tạo role ClubManager.
-- Có thể chạy lại; không xóa bảng hoặc thay đổi mật khẩu hiện tại.
USE [HorseManagement];
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    IF COL_LENGTH('dbo.USER','AccountStatus') IS NULL
        ALTER TABLE dbo.[USER] ADD AccountStatus VARCHAR(16) NOT NULL
            CONSTRAINT DF_USER_AccountStatus DEFAULT ('ACTIVE') WITH VALUES;
    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name='CK_USER_AccountStatus')
        EXEC(N'ALTER TABLE dbo.[USER] ADD CONSTRAINT CK_USER_AccountStatus CHECK (AccountStatus IN (''PENDING'',''ACTIVE'',''DISABLED'',''REJECTED''))');
    IF COL_LENGTH('dbo.Horse','Archived') IS NULL
        ALTER TABLE dbo.Horse ADD Archived BIT NOT NULL CONSTRAINT DF_Horse_Archived DEFAULT (0) WITH VALUES;
    IF COL_LENGTH('dbo.Supply','Archived') IS NULL
        ALTER TABLE dbo.Supply ADD Archived BIT NOT NULL CONSTRAINT DF_Supply_Archived DEFAULT (0) WITH VALUES;
    IF COL_LENGTH('dbo.Supply','Version') IS NULL
        ALTER TABLE dbo.Supply ADD [Version] INT NOT NULL CONSTRAINT DF_Supply_Version DEFAULT (0) WITH VALUES;
    IF COL_LENGTH('dbo.INCIDENTREPORT','Description') IS NULL
        ALTER TABLE dbo.INCIDENTREPORT ADD [Description] NVARCHAR(2000) NULL;

    IF OBJECT_ID('dbo.ApiToken','U') IS NULL
    BEGIN
        CREATE TABLE dbo.ApiToken (
            TokenHash CHAR(64) CONSTRAINT PK_ApiToken PRIMARY KEY,
            UserID INT NOT NULL CONSTRAINT FK_ApiToken_User REFERENCES dbo.[USER](UserID),
            ExpiresAt DATETIME2(3) NOT NULL
        );
        CREATE INDEX IX_ApiToken_User ON dbo.ApiToken(UserID);
        CREATE INDEX IX_ApiToken_Expiry ON dbo.ApiToken(ExpiresAt);
    END;
    -- ERD chưa có thu/chi; bảng này cung cấp nguồn dữ liệu thật cho báo cáo tài chính.
    IF OBJECT_ID('dbo.FinanceEntry','U') IS NULL
    BEGIN
        CREATE TABLE dbo.FinanceEntry (
            EntryID INT IDENTITY(1,1) CONSTRAINT PK_FinanceEntry PRIMARY KEY,
            HorseID INT NULL CONSTRAINT FK_FinanceEntry_Horse REFERENCES dbo.Horse(HorseID),
            EntryType VARCHAR(10) NOT NULL CONSTRAINT CK_FinanceEntry_Type CHECK (EntryType IN ('INCOME','EXPENSE')),
            Amount DECIMAL(18,2) NOT NULL CONSTRAINT CK_FinanceEntry_Amount CHECK (Amount>0),
            Category NVARCHAR(100) NOT NULL,
            [Description] NVARCHAR(1000) NULL,
            EntryDate DATE NOT NULL,
            CreatedBy INT NOT NULL CONSTRAINT FK_FinanceEntry_User REFERENCES dbo.[USER](UserID)
        );
        CREATE INDEX IX_FinanceEntry_Date ON dbo.FinanceEntry(EntryDate);
    END;

    DECLARE @permissions TABLE (PermissionName NVARCHAR(100));
    INSERT @permissions VALUES (N'MANAGE_RBAC'),(N'APPROVE_ACCOUNTS'),(N'VIEW_REPORTS'),
        (N'MANAGE_FINANCE'),(N'VIEW_STABLE'),(N'VIEW_FEEDING'),(N'COMPLETE_CARE'),
        (N'REPORT_INCIDENT'),(N'VIEW_SUPPLIES');
    INSERT dbo.[PERMISSION] (PermissionName)
    SELECT p.PermissionName FROM @permissions p WHERE NOT EXISTS
        (SELECT 1 FROM dbo.[PERMISSION] t WHERE t.PermissionName=p.PermissionName);
    INSERT dbo.ROLEPERMISSION (RoleID,PermissionID)
    SELECT r.RoleID,p.PermissionID FROM dbo.[ROLE] r CROSS JOIN dbo.[PERMISSION] p
    WHERE (r.RoleName=N'Admin' OR (r.RoleName=N'Groom' AND p.PermissionName IN
        (N'VIEW_STABLE',N'VIEW_FEEDING',N'COMPLETE_CARE',N'REPORT_INCIDENT',N'VIEW_SUPPLIES')))
        AND NOT EXISTS (SELECT 1 FROM dbo.ROLEPERMISSION t WHERE t.RoleID=r.RoleID AND t.PermissionID=p.PermissionID);
    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK;
    THROW;
END CATCH;
GO
