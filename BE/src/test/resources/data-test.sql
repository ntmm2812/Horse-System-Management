INSERT INTO dbo.[ROLE] (RoleName) VALUES ('Admin'),('Owner'),('Trainer'),('Vet'),('Groom');
INSERT INTO dbo.[PERMISSION] (PermissionName) VALUES
('MANAGE_USERS'),('VIEW_HORSES'),('MANAGE_HORSES'),('MANAGE_TRAINING'),('MANAGE_MEDICAL'),
('MANAGE_CARE'),('MANAGE_SUPPLIES'),('MANAGE_RACES'),('VIEW_AUDIT_LOG'),('MANAGE_RBAC'),
('APPROVE_ACCOUNTS'),('VIEW_REPORTS'),('MANAGE_FINANCE'),('VIEW_STABLE'),('VIEW_FEEDING'),
('COMPLETE_CARE'),('REPORT_INCIDENT'),('VIEW_SUPPLIES');
INSERT INTO dbo.ROLEPERMISSION (RoleID,PermissionID) SELECT 1,PermissionID FROM dbo.[PERMISSION];
INSERT INTO dbo.ROLEPERMISSION (RoleID,PermissionID) SELECT 5,PermissionID FROM dbo.[PERMISSION] WHERE PermissionID>=14;
INSERT INTO dbo.[USER] (FullName,Email,[Password],RoleID) VALUES
('Admin Test','admin@test.example','!TEST',1),('Groom A','groom@test.example','!TEST',5),
('Groom B','other@test.example','!TEST',5),('Owner','owner@test.example','!TEST',2),
('Trainer','trainer@test.example','!TEST',3),('Vet','vet@test.example','!TEST',4);
INSERT INTO dbo.Horse ([Name],Gender,Age,[Weight],OwnerID) VALUES ('Horse A','Male',5,450,4),('Horse B','Female',4,430,4);
INSERT INTO dbo.HEALTHSTATUS (HorseID,[Status],LastUpdate) VALUES (1,'Healthy',CURRENT_TIMESTAMP),(2,'Healthy',CURRENT_TIMESTAMP);
INSERT INTO dbo.STABLEASSIGNMENT (HorseID,StallNumber,DailyRoutine) VALUES (1,'A-01','Daily care'),(2,'A-02','Daily care');
INSERT INTO dbo.CARETASK (HorseID,GroomID,TaskType,[Status],[Date]) VALUES
(1,2,'Clean stable',0,CURRENT_TIMESTAMP),(2,3,'Feed horse',0,CURRENT_TIMESTAMP),
(1,2,'Future work',0,DATEADD(DAY,1,CURRENT_TIMESTAMP));
INSERT INTO dbo.TRAININGSCHEDULE (HorseID,GroomID,[Date],[Time]) VALUES (1,2,CURRENT_TIMESTAMP,'08:00');
INSERT INTO dbo.FEEDINGPLAN (HorseID,Grain,Grass,Vitamin,Meal) VALUES (1,1.2,3,0.01,'Morning'),(2,1.0,3,0.01,'Morning');
INSERT INTO dbo.Supply (ItemName,[Type],QuantityInStock,ManagedBy) VALUES ('Feed bag','Feed',20,2);
INSERT INTO dbo.TRAININGSESSION (HorseID,HeartRate,Speed,TrainingIndex,[Date]) VALUES (1,100,20,'Good',CURRENT_TIMESTAMP);
