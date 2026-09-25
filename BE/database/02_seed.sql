USE [HorseManagement];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
-- Dữ liệu mẫu: chèn bảng cha trước bảng con theo khóa ngoại; yêu cầu tất cả bảng trống.
-- File đã gộp toàn bộ 950 bản ghi; chạy một lần sau 01_schema.sql.
-- Toàn bộ dữ liệu được chèn trong một giao dịch; có lỗi thì hoàn tác tất cả.
-- PHẦN 1: Vai trò, quyền và dữ liệu nghiệp vụ ban đầu.
BEGIN TRY
    BEGIN TRANSACTION;
    DECLARE @table SYSNAME, @check NVARCHAR(MAX);
    DECLARE empty_tables CURSOR LOCAL FAST_FORWARD FOR
        SELECT name FROM sys.tables WHERE schema_id = SCHEMA_ID(N'dbo');
    OPEN empty_tables;
    FETCH NEXT FROM empty_tables INTO @table;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @check = N'IF EXISTS (SELECT 1 FROM dbo.' + QUOTENAME(@table)
            + N') THROW 50002, ''Seed requires empty tables; existing data was not changed.'', 1;';
        EXEC sys.sp_executesql @check;
        FETCH NEXT FROM empty_tables INTO @table;
    END;
    CLOSE empty_tables;
    DEALLOCATE empty_tables;

    SET IDENTITY_INSERT dbo.[ROLE] ON;
    INSERT dbo.[ROLE] (RoleID, RoleName) VALUES
        (1, N'Admin'), (2, N'Owner'), (3, N'Trainer'), (4, N'Vet'), (5, N'Groom');
    SET IDENTITY_INSERT dbo.[ROLE] OFF;

    SET IDENTITY_INSERT dbo.[PERMISSION] ON;
    INSERT dbo.[PERMISSION] (PermissionID, PermissionName) VALUES
        (1, N'MANAGE_USERS'), (2, N'VIEW_HORSES'), (3, N'MANAGE_HORSES'),
        (4, N'MANAGE_TRAINING'), (5, N'MANAGE_MEDICAL'), (6, N'MANAGE_CARE'),
        (7, N'MANAGE_SUPPLIES'), (8, N'MANAGE_RACES'), (9, N'VIEW_AUDIT_LOG');
    SET IDENTITY_INSERT dbo.[PERMISSION] OFF;

    INSERT dbo.ROLEPERMISSION (RoleID, PermissionID) VALUES
        (1,1), (1,2), (1,3), (1,4), (1,5), (1,6), (1,7), (1,8), (1,9),
        (2,2), (2,8), (3,2), (3,4), (4,2), (4,5), (5,2), (5,6), (5,7);

    -- Giá trị mật khẩu mẫu không dùng để đăng nhập; ứng dụng cần tạo mã băm thật.
    SET IDENTITY_INSERT dbo.[USER] ON;
    INSERT dbo.[USER] (UserID, FullName, Email, [Password], RoleID) VALUES
        (1, N'Nguyễn Minh An', 'admin@example.com', '!DEMO_ACCOUNT_NO_LOGIN_01', 1),
        (2, N'Trần Hoàng Nam', 'owner.nam@example.com', '!DEMO_ACCOUNT_NO_LOGIN_02', 2),
        (3, N'Lê Thu Hà', 'owner.ha@example.com', '!DEMO_ACCOUNT_NO_LOGIN_03', 2),
        (4, N'Phạm Quốc Huy', 'trainer.huy@example.com', '!DEMO_ACCOUNT_NO_LOGIN_04', 3),
        (5, N'Võ Ngọc Mai', 'vet.mai@example.com', '!DEMO_ACCOUNT_NO_LOGIN_05', 4),
        (6, N'Đặng Thanh Tùng', 'groom.tung@example.com', '!DEMO_ACCOUNT_NO_LOGIN_06', 5),
        (7, N'Bùi Thảo Vy', 'groom.vy@example.com', '!DEMO_ACCOUNT_NO_LOGIN_07', 5);
    SET IDENTITY_INSERT dbo.[USER] OFF;

    SET IDENTITY_INSERT dbo.Horse ON;
    INSERT dbo.Horse (HorseID, [Name], Gender, Age, [Weight], Lineage, OwnerID) VALUES
        (1, N'Sấm', N'Male', 5, 480.50, N'Thoroughbred', 2),
        (2, N'Bạch Vân', N'Female', 4, 435.00, N'Arabian', 2),
        (3, N'Ánh Dương', N'Female', 6, 460.25, N'Quarter Horse', 3),
        (4, N'Hắc Phong', N'Male', 7, 510.00, N'Thoroughbred', 3),
        (5, N'Sao Mai', N'Female', 3, 405.75, N'Arabian', 2);
    SET IDENTITY_INSERT dbo.Horse OFF;

    SET IDENTITY_INSERT dbo.TRAININGPLAN ON;
    INSERT dbo.TRAININGPLAN (PlanID, HorseID, Distance, Workload, Phase, CreatedBy) VALUES
        (1,1,3.50,45,N'Endurance',4), (2,2,2.00,30,N'Foundation',4),
        (3,3,2.50,35,N'Conditioning',4), (4,4,4.00,50,N'Competition',4),
        (5,5,1.00,20,N'Foundation',4);
    SET IDENTITY_INSERT dbo.TRAININGPLAN OFF;

    SET IDENTITY_INSERT dbo.TRAININGSCHEDULE ON;
    INSERT dbo.TRAININGSCHEDULE (ScheduleID, HorseID, GroomID, [Date], [Time]) VALUES
        (1,1,6,'20260926','06:00'), (2,2,7,'20260926','06:00'),
        (3,3,6,'20260926','07:00'), (4,4,7,'20260926','07:00'),
        (5,5,6,'20260924','08:00');
    SET IDENTITY_INSERT dbo.TRAININGSCHEDULE OFF;

    SET IDENTITY_INSERT dbo.TRAININGSESSION ON;
    INSERT dbo.TRAININGSESSION (SessionID, HorseID, HeartRate, Speed, TrainingIndex, [Comment], [Date]) VALUES
        (1,1,110,28.50,N'Good',N'Hoàn thành buổi tập theo kế hoạch.','20260924'),
        (2,2,100,22.00,N'Good',N'Giữ nhịp ổn định.','20260924'),
        (3,3,105,24.50,N'Average',N'Cần theo dõi thêm ở buổi tập sau.','20260924'),
        (4,4,120,32.00,N'Good',N'Hoàn thành bài tập tốc độ.','20260924'),
        (5,5,95,18.00,N'Good',N'Làm quen bài tập cơ bản.','20260924');
    SET IDENTITY_INSERT dbo.TRAININGSESSION OFF;

    SET IDENTITY_INSERT dbo.STABLEASSIGNMENT ON;
    INSERT dbo.STABLEASSIGNMENT (StableID, HorseID, StallNumber, DailyRoutine) VALUES
        (1,1,N'A-01',N'Vệ sinh chuồng và kiểm tra nước uống mỗi sáng.'),
        (2,2,N'A-02',N'Chải lông và kiểm tra dụng cụ.'),
        (3,3,N'A-03',N'Vệ sinh chuồng và thay chất độn.'),
        (4,4,N'B-01',N'Kiểm tra chuồng trước và sau buổi tập.'),
        (5,5,N'B-02',N'Ghi nhận lượng thức ăn còn lại.');
    SET IDENTITY_INSERT dbo.STABLEASSIGNMENT OFF;

    SET IDENTITY_INSERT dbo.RACE ON;
    INSERT dbo.RACE (RaceID, RaceName, [Date], [Time]) VALUES
        (1,N'Giải giao hữu tháng 10','20261010','08:00'),
        (2,N'Cúp mùa thu','20261017','08:30'),
        (3,N'Giải câu lạc bộ mở rộng','20261101','09:00');
    SET IDENTITY_INSERT dbo.RACE OFF;

    INSERT dbo.RACEREGISTRATION (HorseID, RaceID, RegistrationDate) VALUES
        (1,1,'20260925'), (2,1,'20260925'), (3,1,'20260925'),
        (4,2,'20260925'), (1,2,'20260925'), (5,3,'20260925');

    SET IDENTITY_INSERT dbo.MEDICALRECORD ON;
    INSERT dbo.MEDICALRECORD (RecordID, HorseID, VetID, Diagnosis, Treatment, InjuryLocation, [Date]) VALUES
        (1,1,5,N'Kiểm tra định kỳ.',N'Ghi nhận và theo dõi.',NULL,'20260920'),
        (2,2,5,N'Kiểm tra định kỳ.',N'Ghi nhận và theo dõi.',NULL,'20260920'),
        (3,3,5,N'Trầy xước nhẹ đã được kiểm tra.',N'Theo dõi theo hồ sơ bác sĩ.',N'Chân trước trái','20260918'),
        (4,4,5,N'Kiểm tra sau vận động.',N'Ghi nhận và theo dõi.',NULL,'20260920'),
        (5,5,5,N'Kiểm tra định kỳ.',N'Ghi nhận và theo dõi.',NULL,'20260920');
    SET IDENTITY_INSERT dbo.MEDICALRECORD OFF;

    SET IDENTITY_INSERT dbo.TRAININGLOCK ON;
    INSERT dbo.TRAININGLOCK (LockID, HorseID, VetID, Reason, StartDate, EndDate) VALUES
        (1,1,5,N'Tạm nghỉ để kiểm tra định kỳ.','20260910','20260910'),
        (2,3,5,N'Tạm nghỉ để theo dõi vết trầy.','20260918','20260920'),
        (3,5,5,N'Tạm nghỉ chờ đánh giá lại.','20260925',NULL);
    SET IDENTITY_INSERT dbo.TRAININGLOCK OFF;

    -- Ngựa số 5 đã tập trước ngày bắt đầu khóa tập chưa xác định ngày kết thúc (25/09).
    SET IDENTITY_INSERT dbo.CARETASK ON;
    INSERT dbo.CARETASK (TaskID, HorseID, GroomID, TaskType, [Status], [Date]) VALUES
        (1,1,6,N'Vệ sinh chuồng',1,'20260925'),
        (2,2,7,N'Chải lông',1,'20260925'),
        (3,3,6,N'Kiểm tra nước uống',0,'20260925'),
        (4,4,7,N'Vệ sinh dụng cụ',0,'20260925'),
        (5,5,6,N'Quan sát và ghi nhận tình trạng',0,'20260925');
    SET IDENTITY_INSERT dbo.CARETASK OFF;

    INSERT dbo.HEALTHSTATUS (HorseID, [Status], LastUpdate) VALUES
        (1,N'Healthy','20260925'), (2,N'Healthy','20260925'),
        (3,N'Healthy','20260925'), (4,N'Healthy','20260925'),
        (5,N'UnderObservation','20260925');

    SET IDENTITY_INSERT dbo.VACCINATIONSCHEDULE ON;
    INSERT dbo.VACCINATIONSCHEDULE (ScheduleID, HorseID, [Type], ScheduleDate, [Status]) VALUES
        (1,1,N'Lịch tiêm mẫu A','20261005',N'Scheduled'),
        (2,2,N'Lịch tiêm mẫu A','20261005',N'Scheduled'),
        (3,3,N'Lịch tiêm mẫu B','20260915',N'Completed'),
        (4,4,N'Lịch tiêm mẫu B','20261006',N'Scheduled'),
        (5,5,N'Lịch tiêm mẫu A','20261007',N'Scheduled');
    SET IDENTITY_INSERT dbo.VACCINATIONSCHEDULE OFF;

    -- Định lượng giả lập phục vụ minh họa phần mềm, đơn vị kg mỗi bữa.
    SET IDENTITY_INSERT dbo.FEEDINGPLAN ON;
    INSERT dbo.FEEDINGPLAN (FeedID, HorseID, Grain, Grass, Vitamin, Meal) VALUES
        (1,1,1.500,3.000,0.010,N'Morning'), (2,1,1.000,3.000,0.000,N'Evening'),
        (3,2,1.000,2.500,0.010,N'Morning'), (4,3,1.200,2.800,0.010,N'Morning'),
        (5,4,1.800,3.200,0.010,N'Morning'), (6,5,0.800,2.000,0.005,N'Morning');
    SET IDENTITY_INSERT dbo.FEEDINGPLAN OFF;

    SET IDENTITY_INSERT dbo.Supply ON;
    INSERT dbo.Supply (SupplyID, ItemName, [Type], QuantityInStock, ManagedBy) VALUES
        (1,N'Bao thức ăn hỗn hợp',N'Feed',50,6),
        (2,N'Kiện cỏ khô',N'Feed',120,6),
        (3,N'Hộp vitamin mẫu',N'Supplement',20,7),
        (4,N'Bàn chải lông',N'Equipment',15,7),
        (5,N'Bộ vệ sinh chuồng',N'Equipment',10,6);
    SET IDENTITY_INSERT dbo.Supply OFF;

    SET IDENTITY_INSERT dbo.INCIDENTREPORT ON;
    INSERT dbo.INCIDENTREPORT (ReportID, HorseID, GroomID, IncidentType, ImagePath, [Date]) VALUES
        (1,3,6,N'Trầy xước nhẹ',NULL,'20260918'),
        (2,1,6,N'Hỏng khóa cửa chuồng',NULL,'20260919'),
        (3,5,7,N'Ăn ít hơn thường lệ',NULL,'20260925');
    SET IDENTITY_INSERT dbo.INCIDENTREPORT OFF;

    SET IDENTITY_INSERT dbo.AuditLog ON;
    INSERT dbo.AuditLog (LogID, UserID, [Action], TargetTable, TargetID, [TimeStamp]) VALUES
        (1,1,N'CREATE',N'Horse',1,'2026-09-01T08:00:00'),
        (2,4,N'CREATE',N'TRAININGPLAN',1,'2026-09-21T09:00:00'),
        (3,5,N'CREATE',N'MEDICALRECORD',3,'2026-09-18T10:00:00'),
        (4,6,N'UPDATE',N'CARETASK',1,'2026-09-25T07:00:00'),
        (5,7,N'CREATE',N'INCIDENTREPORT',3,'2026-09-25T07:30:00');
    SET IDENTITY_INSERT dbo.AuditLog OFF;

    -- PHẦN 2: Thêm người dùng, ngựa và các hồ sơ nghiệp vụ mở rộng.
    -- Khóa giao dịch để tránh hai phiên chèn đồng thời cùng bộ dữ liệu mở rộng.
    DECLARE @SeedLock INT;
    EXEC @SeedLock = sys.sp_getapplock @Resource = N'HorseManagement.ExtendedDemoSeed',
        @LockMode = 'Exclusive', @LockOwner = 'Transaction', @LockTimeout = 10000;
    IF @SeedLock < 0 THROW 50020, 'Could not acquire demo seed lock.', 1;

    IF (SELECT COUNT(*) FROM dbo.[ROLE] WHERE RoleName IN (N'Owner',N'Trainer',N'Vet',N'Groom')) <> 4
        THROW 50021, 'Run 02_seed.sql first to create the required roles.', 1;

    DECLARE @Users TABLE (
        DemoNo INT PRIMARY KEY, FullName NVARCHAR(100), Email VARCHAR(254),
        RoleName NVARCHAR(50), UserID INT
    );
    INSERT @Users (DemoNo, FullName, Email, RoleName) VALUES
        (1,N'Nguyễn Đức Long','demo.owner.long@example.com',N'Owner'),
        (2,N'Trần Ngọc Lan','demo.owner.lan@example.com',N'Owner'),
        (3,N'Lê Minh Khang','demo.owner.khang@example.com',N'Owner'),
        (4,N'Phạm Bảo Trâm','demo.owner.tram@example.com',N'Owner'),
        (5,N'Hoàng Tuấn Kiệt','demo.owner.kiet@example.com',N'Owner'),
        (6,N'Vũ Thanh Hương','demo.owner.huong@example.com',N'Owner'),
        (7,N'Đỗ Gia Bảo','demo.owner.bao@example.com',N'Owner'),
        (8,N'Bùi Khánh Linh','demo.owner.linh@example.com',N'Owner'),
        (9,N'Nguyễn Thành Đạt','demo.trainer.dat@example.com',N'Trainer'),
        (10,N'Trần Hải Đăng','demo.trainer.dang@example.com',N'Trainer'),
        (11,N'Lê Phương Anh','demo.trainer.anh@example.com',N'Trainer'),
        (12,N'Phạm Thùy Dung','demo.vet.dung@example.com',N'Vet'),
        (13,N'Hoàng Minh Đức','demo.vet.duc@example.com',N'Vet'),
        (14,N'Võ Bích Ngọc','demo.vet.ngoc@example.com',N'Vet'),
        (15,N'Đặng Văn Phúc','demo.groom.phuc@example.com',N'Groom'),
        (16,N'Bùi Nhật Minh','demo.groom.minh@example.com',N'Groom'),
        (17,N'Đỗ Mỹ Duyên','demo.groom.duyen@example.com',N'Groom'),
        (18,N'Ngô Quốc Bảo','demo.groom.bao@example.com',N'Groom');

    INSERT dbo.[USER] (FullName, Email, [Password], RoleID)
    SELECT u.FullName, u.Email, '!DEMO_ACCOUNT_NO_LOGIN_EXTENDED', r.RoleID
    FROM @Users u JOIN dbo.[ROLE] r ON r.RoleName = u.RoleName
    WHERE NOT EXISTS (SELECT 1 FROM dbo.[USER] t WHERE t.Email = u.Email);
    UPDATE u SET UserID = t.UserID FROM @Users u JOIN dbo.[USER] t ON t.Email = u.Email;
    IF EXISTS (SELECT 1 FROM @Users u JOIN dbo.[USER] t ON t.UserID = u.UserID
        JOIN dbo.[ROLE] r ON r.RoleID = t.RoleID WHERE r.RoleName <> u.RoleName)
        THROW 50022, 'A demo email belongs to an unexpected role. No data was changed.', 1;

    DECLARE @Horses TABLE (
        DemoNo INT PRIMARY KEY, HorseName NVARCHAR(100), Gender NVARCHAR(20),
        Age INT, [Weight] DECIMAL(7,2), Lineage NVARCHAR(255),
        OwnerID INT, TrainerID INT, VetID INT, GroomID INT, HorseID INT
    );
    INSERT @Horses (DemoNo,HorseName,Gender,Age,[Weight],Lineage) VALUES
        (1,N'Xích Thố',N'Male',5,485.50,N'Thoroughbred'),
        (2,N'Ngân Hà',N'Female',4,430.00,N'Arabian'),
        (3,N'Phi Vân',N'Male',6,498.25,N'Quarter Horse'),
        (4,N'Bình Minh',N'Female',3,410.50,N'Arabian'),
        (5,N'Phong Vũ',N'Male',7,520.00,N'Thoroughbred'),
        (6,N'Tuyết Mai',N'Female',5,445.75,N'Warmblood'),
        (7,N'Đại Phong',N'Male',4,470.00,N'Quarter Horse'),
        (8,N'Kim Sa',N'Female',6,460.50,N'Arabian'),
        (9,N'Thiên Mã',N'Male',5,505.25,N'Thoroughbred'),
        (10,N'Ngọc Sương',N'Female',4,425.50,N'Warmblood'),
        (11,N'Hải Âu',N'Male',3,415.00,N'Arabian'),
        (12,N'Thanh Vân',N'Female',7,480.25,N'Quarter Horse'),
        (13,N'Lôi Đình',N'Male',6,515.00,N'Thoroughbred'),
        (14,N'Thu Nguyệt',N'Female',5,450.00,N'Warmblood'),
        (15,N'Hoàng Hôn',N'Male',4,465.50,N'Quarter Horse'),
        (16,N'Mây Hồng',N'Female',3,405.25,N'Arabian'),
        (17,N'Bão Táp',N'Male',8,530.00,N'Thoroughbred'),
        (18,N'Sương Sớm',N'Female',6,455.75,N'Warmblood'),
        (19,N'Trường Sơn',N'Male',5,490.50,N'Quarter Horse'),
        (20,N'Ngọc Lan',N'Female',4,440.00,N'Arabian');
    UPDATE h SET OwnerID = o.UserID, TrainerID = t.UserID, VetID = v.UserID, GroomID = g.UserID
    FROM @Horses h JOIN @Users o ON o.DemoNo = 1 + (h.DemoNo-1)%8
    JOIN @Users t ON t.DemoNo = 9 + (h.DemoNo-1)%3
    JOIN @Users v ON v.DemoNo = 12 + (h.DemoNo-1)%3
    JOIN @Users g ON g.DemoNo = 15 + (h.DemoNo-1)%4;
    INSERT dbo.Horse ([Name],Gender,Age,[Weight],Lineage,OwnerID)
    SELECT HorseName,Gender,Age,[Weight],Lineage,OwnerID FROM @Horses h
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Horse t WHERE t.[Name]=h.HorseName AND t.OwnerID=h.OwnerID);
    IF EXISTS (SELECT 1 FROM @Horses h JOIN dbo.Horse t ON t.[Name]=h.HorseName AND t.OwnerID=h.OwnerID
        GROUP BY h.DemoNo HAVING COUNT(*) > 1)
        THROW 50023, 'Ambiguous demo horse names for the same owner. No data was changed.', 1;
    UPDATE h SET HorseID=t.HorseID FROM @Horses h
    JOIN dbo.Horse t ON t.[Name]=h.HorseName AND t.OwnerID=h.OwnerID;

    DECLARE @Days TABLE (DayNo INT PRIMARY KEY);
    INSERT @Days VALUES (0),(1),(2),(3),(4),(5),(6),(7);

    INSERT dbo.TRAININGPLAN (HorseID,Distance,Workload,Phase,CreatedBy)
    SELECT h.HorseID, 1.5+h.DemoNo%4+p.Stage, 20+5*(h.DemoNo%4)+10*p.Stage,
        p.Phase,h.TrainerID
    FROM @Horses h CROSS JOIN (VALUES (0,N'Foundation'),(1,N'Endurance')) p(Stage,Phase)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.TRAININGPLAN t WHERE t.HorseID=h.HorseID AND t.Phase=p.Phase);

    -- Mỗi ngựa có tám buổi tập đã diễn ra trước thời gian khóa tập bên dưới.
    -- Bốn nhân viên, mỗi người phụ trách năm ngựa ở các khung giờ khác nhau.
    INSERT dbo.TRAININGSCHEDULE (HorseID,GroomID,[Date],[Time])
    SELECT h.HorseID,h.GroomID,DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260901')),
        TIMEFROMPARTS(6+(h.DemoNo-1)/4,0,0,0,0)
    FROM @Horses h CROSS JOIN @Days d
    WHERE NOT EXISTS (SELECT 1 FROM dbo.TRAININGSCHEDULE t WHERE t.HorseID=h.HorseID
        AND t.[Date]=DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260901'))
        AND t.[Time]=TIMEFROMPARTS(6+(h.DemoNo-1)/4,0,0,0,0));

    INSERT dbo.TRAININGSESSION (HorseID,HeartRate,Speed,TrainingIndex,[Comment],[Date])
    SELECT h.HorseID,90+h.DemoNo%25+d.DayNo*2,18+h.DemoNo%12+d.DayNo*0.5,
        CASE WHEN d.DayNo%3=0 THEN N'Average' ELSE N'Good' END,
        CASE d.DayNo%4 WHEN 0 THEN N'Làm quen đường tập và giữ nhịp.'
            WHEN 1 THEN N'Hoàn thành bài tập sức bền.' WHEN 2 THEN N'Tập chuyển hướng theo hiệu lệnh.'
            ELSE N'Hoàn thành bài tập, ghi nhận tiến bộ.' END,
        DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260901'))
    FROM @Horses h CROSS JOIN @Days d
    WHERE NOT EXISTS (SELECT 1 FROM dbo.TRAININGSESSION t WHERE t.HorseID=h.HorseID
        AND t.[Date]=DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260901')));

    INSERT dbo.STABLEASSIGNMENT (HorseID,StallNumber,DailyRoutine)
    SELECT h.HorseID,CONCAT(N'C-',RIGHT('00'+CONVERT(VARCHAR(2),h.DemoNo),2)),
        N'06:00 kiểm tra nước; 11:00 vệ sinh chuồng; 17:00 chải lông và ghi sổ.'
    FROM @Horses h WHERE NOT EXISTS (SELECT 1 FROM dbo.STABLEASSIGNMENT t WHERE t.HorseID=h.HorseID);

    DECLARE @Races TABLE (DemoNo INT PRIMARY KEY, RaceName NVARCHAR(150), RaceDate DATE, RaceID INT);
    INSERT @Races (DemoNo,RaceName,RaceDate) VALUES
        (1,N'Cúp Bình Minh 2026','20261107'), (2,N'Giải Sức Bền 2026','20261114'),
        (3,N'Cúp Cao Nguyên 2026','20261121'), (4,N'Giải Tốc Độ 2026','20261128'),
        (5,N'Cúp Giao Hữu Miền Nam 2026','20261205'),
        (6,N'Giải Ngựa Trẻ 2026','20261212'), (7,N'Cúp Cuối Năm 2026','20261219');
    INSERT dbo.RACE (RaceName,[Date],[Time])
    SELECT r.RaceName,r.RaceDate,'08:00' FROM @Races r
    WHERE NOT EXISTS (SELECT 1 FROM dbo.RACE t WHERE t.RaceName=r.RaceName AND t.[Date]=r.RaceDate);
    IF EXISTS (SELECT 1 FROM @Races r JOIN dbo.RACE t ON t.RaceName=r.RaceName AND t.[Date]=r.RaceDate
        GROUP BY r.DemoNo HAVING COUNT(*)>1)
        THROW 50024, 'Ambiguous demo race names and dates. No data was changed.', 1;
    UPDATE r SET RaceID=t.RaceID FROM @Races r JOIN dbo.RACE t ON t.RaceName=r.RaceName AND t.[Date]=r.RaceDate;
    INSERT dbo.RACEREGISTRATION (HorseID,RaceID,RegistrationDate)
    SELECT h.HorseID,r.RaceID,'20261001' FROM @Horses h
    CROSS JOIN (VALUES (0),(1),(2)) n(OffsetNo)
    JOIN @Races r ON r.DemoNo=1+(h.DemoNo-1+n.OffsetNo)%7
    WHERE NOT EXISTS (SELECT 1 FROM dbo.RACEREGISTRATION t WHERE t.HorseID=h.HorseID AND t.RaceID=r.RaceID);

    INSERT dbo.MEDICALRECORD (HorseID,VetID,Diagnosis,Treatment,InjuryLocation,[Date])
    SELECT h.HorseID,h.VetID,N'Kiểm tra định kỳ, ghi nhận thể trạng.',
        N'Lưu hồ sơ theo dõi định kỳ.',NULL,dt.ExamDate
    FROM @Horses h CROSS JOIN (VALUES (CONVERT(DATE,'20260828')),(CONVERT(DATE,'20260922'))) dt(ExamDate)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.MEDICALRECORD t WHERE t.HorseID=h.HorseID AND t.[Date]=dt.ExamDate);
    INSERT dbo.TRAININGLOCK (HorseID,VetID,Reason,StartDate,EndDate)
    SELECT h.HorseID,h.VetID,N'Tạm nghỉ theo dõi sau kiểm tra; đã kết thúc.','20260918','20260920'
    FROM @Horses h WHERE h.DemoNo%4=0
    AND NOT EXISTS (SELECT 1 FROM dbo.TRAININGLOCK t WHERE t.HorseID=h.HorseID AND t.StartDate='20260918');

    INSERT dbo.CARETASK (HorseID,GroomID,TaskType,[Status],[Date])
    SELECT h.HorseID,h.GroomID,
        CASE d.DayNo%3 WHEN 0 THEN N'Vệ sinh chuồng' WHEN 1 THEN N'Chải lông' ELSE N'Kiểm tra nước uống' END,
        CASE WHEN d.DayNo<6 THEN 1 ELSE 0 END,DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260919'))
    FROM @Horses h CROSS JOIN @Days d WHERE d.DayNo<7
    AND NOT EXISTS (SELECT 1 FROM dbo.CARETASK t WHERE t.HorseID=h.HorseID
        AND t.[Date]=DATEADD(DAY,d.DayNo,CONVERT(DATE,'20260919'))
        AND t.TaskType=CASE d.DayNo%3 WHEN 0 THEN N'Vệ sinh chuồng' WHEN 1 THEN N'Chải lông' ELSE N'Kiểm tra nước uống' END);
    INSERT dbo.HEALTHSTATUS (HorseID,[Status],LastUpdate)
    SELECT h.HorseID,N'Healthy','20260925' FROM @Horses h
    WHERE NOT EXISTS (SELECT 1 FROM dbo.HEALTHSTATUS t WHERE t.HorseID=h.HorseID);

    INSERT dbo.VACCINATIONSCHEDULE (HorseID,[Type],ScheduleDate,[Status])
    SELECT h.HorseID,v.VaccineType,v.DueDate,v.VaccineStatus FROM @Horses h
    CROSS JOIN (VALUES (N'Lịch tiêm mẫu A',CONVERT(DATE,'20260915'),N'Completed'),
        (N'Lịch tiêm mẫu B',CONVERT(DATE,'20261015'),N'Scheduled')) v(VaccineType,DueDate,VaccineStatus)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.VACCINATIONSCHEDULE t WHERE t.HorseID=h.HorseID
        AND t.[Type]=v.VaccineType AND t.ScheduleDate=v.DueDate);
    -- Định lượng giả lập để minh họa phần mềm, đơn vị kg mỗi bữa.
    INSERT dbo.FEEDINGPLAN (HorseID,Grain,Grass,Vitamin,Meal)
    SELECT h.HorseID,0.8+0.1*(h.DemoNo%5),2.0+0.2*(h.DemoNo%4),
        CASE WHEN m.Meal=N'Morning' THEN 0.010 ELSE 0 END,m.Meal
    FROM @Horses h CROSS JOIN (VALUES (N'Morning'),(N'Noon'),(N'Evening')) m(Meal)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.FEEDINGPLAN t WHERE t.HorseID=h.HorseID AND t.Meal=m.Meal);

    INSERT dbo.Supply (ItemName,[Type],QuantityInStock,ManagedBy)
    SELECT s.ItemName,s.ItemType,s.Quantity,u.UserID FROM (VALUES
        (1,N'Bao yến mạch mẫu',N'Feed',80),(2,N'Bao cám viên mẫu',N'Feed',60),
        (3,N'Kiện cỏ alfalfa mẫu',N'Feed',95),(4,N'Kiện cỏ timothy mẫu',N'Feed',100),
        (5,N'Xô đựng nước',N'Equipment',30),(6,N'Máng ăn treo',N'Equipment',25),
        (7,N'Dây dắt ngựa',N'Equipment',35),(8,N'Bàn chải mềm',N'Equipment',22),
        (9,N'Lược chải bờm',N'Equipment',18),(10,N'Khăn lau ngựa',N'Equipment',50),
        (11,N'Bao chất độn chuồng',N'Stable',75),(12,N'Chổi vệ sinh chuồng',N'Stable',16),
        (13,N'Xe đẩy thức ăn',N'Equipment',8),(14,N'Thùng đựng dụng cụ',N'Equipment',12),
        (15,N'Găng tay chăm sóc',N'Equipment',40)) s(DemoNo,ItemName,ItemType,Quantity)
    JOIN @Users u ON u.DemoNo=15+(s.DemoNo-1)%4
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Supply t WHERE t.ItemName=s.ItemName AND t.[Type]=s.ItemType);

    INSERT dbo.INCIDENTREPORT (HorseID,GroomID,IncidentType,ImagePath,[Date])
    SELECT h.HorseID,h.GroomID,
        CASE WHEN h.DemoNo%4=0 THEN N'Cần theo dõi sau kiểm tra' ELSE N'Hỏng máng nước, đã báo sửa' END,
        NULL,'20260918'
    FROM @Horses h WHERE h.DemoNo%2=0
    AND NOT EXISTS (SELECT 1 FROM dbo.INCIDENTREPORT t WHERE t.HorseID=h.HorseID AND t.[Date]='20260918');
    INSERT dbo.AuditLog (UserID,[Action],TargetTable,TargetID,[TimeStamp])
    SELECT h.OwnerID,N'CREATE',N'Horse',h.HorseID,'2026-08-25T08:00:00' FROM @Horses h
    WHERE NOT EXISTS (SELECT 1 FROM dbo.AuditLog t WHERE t.TargetTable=N'Horse'
        AND t.TargetID=h.HorseID AND t.[Action]=N'CREATE');


    COMMIT TRANSACTION;
    PRINT N'Đã thêm đủ 950 bản ghi mẫu vào 20 bảng.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    -- Tắt IDENTITY_INSERT trong phiên hiện tại nếu có lỗi khi chèn dữ liệu mẫu.
    DECLARE @cleanup NVARCHAR(MAX) = N'';
    SELECT @cleanup = @cleanup + N'SET IDENTITY_INSERT dbo.' + QUOTENAME(t.name) + N' OFF;'
    FROM sys.tables t
    WHERE t.schema_id = SCHEMA_ID(N'dbo') AND EXISTS
        (SELECT 1 FROM sys.identity_columns i WHERE i.object_id = t.object_id);
    EXEC sys.sp_executesql @cleanup;
    THROW;
END CATCH;
GO
