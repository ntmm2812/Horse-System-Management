USE [HorseManagement];
GO
SET NOCOUNT ON;

DECLARE @expected TABLE (TableName SYSNAME PRIMARY KEY, SeedRows INT);
INSERT @expected VALUES
    (N'ROLE',5), (N'PERMISSION',9), (N'USER',25), (N'ROLEPERMISSION',18),
    (N'Horse',25), (N'TRAININGPLAN',45), (N'TRAININGSCHEDULE',165),
    (N'TRAININGSESSION',165), (N'STABLEASSIGNMENT',25), (N'RACE',10),
    (N'RACEREGISTRATION',66), (N'MEDICALRECORD',45), (N'TRAININGLOCK',8),
    (N'CARETASK',145), (N'HEALTHSTATUS',25), (N'VACCINATIONSCHEDULE',45),
    (N'FEEDINGPLAN',66), (N'Supply',20), (N'AuditLog',25), (N'INCIDENTREPORT',13);

IF EXISTS (SELECT 1 FROM @expected WHERE OBJECT_ID(N'dbo.' + QUOTENAME(TableName), N'U') IS NULL)
    THROW 50003, 'One or more expected tables are missing.', 1;
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE is_disabled = 1 OR is_not_trusted = 1)
    THROW 50004, 'A foreign key is disabled or untrusted.', 1;
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE is_disabled = 1 OR is_not_trusted = 1)
    THROW 50005, 'A check constraint is disabled or untrusted.', 1;

DECLARE @counts TABLE (TableName SYSNAME, ActualRows BIGINT);
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql = @sql + CASE WHEN @sql = N'' THEN N'' ELSE N' UNION ALL ' END
    + N'SELECT N''' + TableName + N''', COUNT_BIG(*) FROM dbo.' + QUOTENAME(TableName)
FROM @expected;
INSERT @counts EXEC sys.sp_executesql @sql;
SELECT e.TableName, c.ActualRows, e.SeedRows AS ExpectedImmediatelyAfterSeed
FROM @expected e JOIN @counts c ON c.TableName = e.TableName ORDER BY e.TableName;
IF EXISTS (SELECT 1 FROM @counts WHERE ActualRows = 0)
    THROW 50006, 'An expected table has no rows. Run the seed on an empty installation.', 1;

DBCC CHECKCONSTRAINTS WITH ALL_CONSTRAINTS;
SELECT COUNT(*) AS ForeignKeyCount FROM sys.foreign_keys;
SELECT SUM(ActualRows) AS TotalRows FROM @counts;
PRINT N'Verified: all 20 tables contain data; constraints are enabled and trusted.';
GO
