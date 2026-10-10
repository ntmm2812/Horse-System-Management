-- =====================================================================
-- HORSE MANAGEMENT - TEAM SAMPLE DATA (ADAPTED TO YOUR SQL SERVER SCHEMA)
-- Original sample rows were extracted from horse_management_sqlserver(1).sql.
-- This file contains DATA ONLY; it does NOT create/drop tables or databases.
-- Target: horse_management_sqlserver_fixed.sql (40-table schema).
--
-- Changes needed for compatibility:
--   users.password -> users.password_hash; all 20 demo accounts use the
--   same login password 12345, stored as individually generated BCrypt hashes.
--   horses.owner_name / manager_name removed; target schema derives them by join.
--   Existing master seed (roles, permissions, role_permissions, supply_categories)
--   is reused rather than inserted twice.
--
-- IMPORTANT: This seed assumes the target schema's ORIGINAL master data
-- was inserted exactly once with role IDs 1..5 and category IDs 1..6.
-- All 36 non-master tables must be empty, with IDENTITY values still starting at 1.
-- Do not run after horse_management_seed_demo.sql or after partial seed attempts.
-- All 20 demo user accounts have status ACTIVE (local development only).
-- =====================================================================
USE horse_management;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Ensure we're applying to the intended schema and expected seeded IDs.
    IF COL_LENGTH('dbo.users', 'password_hash') IS NULL
        THROW 51001, 'Missing users.password_hash: wrong schema/version.', 1;
    IF COL_LENGTH('dbo.horses', 'owner_name') IS NOT NULL
        THROW 51002, 'Found horses.owner_name: this is team schema, not target schema.', 1;
    IF (SELECT COUNT(*) FROM dbo.roles) <> 5
       OR (SELECT COUNT(*) FROM dbo.permissions) <> 22
       OR (SELECT COUNT(*) FROM dbo.supply_categories) <> 6
       OR (SELECT COUNT(*) FROM dbo.role_permissions) <> 51
        THROW 51003, 'Expected original master seed (5 roles, 22 permissions, 51 role-permission mappings, 6 categories).', 1;
    IF EXISTS (
        SELECT 1 FROM (VALUES
          (1,N'HEAD_TRAINER'),(2,N'VETERINARIAN'),(3,N'GROOM'),
          (4,N'HORSE_OWNER'),(5,N'CLUB_MANAGER')
        ) r(id,code)
        WHERE NOT EXISTS (SELECT 1 FROM dbo.roles d WHERE d.role_id=r.id AND d.role_code=r.code)
    ) THROW 51004, 'Role identity IDs do not match the source insert data.', 1;
    IF EXISTS (
        SELECT 1 FROM (VALUES
          (1,N'Ngũ cốc'),(2,N'Cỏ khô'),(3,N'Vitamin & Khoáng'),
          (4,N'Thuốc'),(5,N'Vắc-xin'),(6,N'Dụng cụ')
        ) c(id,name)
        WHERE NOT EXISTS (SELECT 1 FROM dbo.supply_categories d
                          WHERE d.category_id=c.id AND d.category_name=c.name)
    ) THROW 51005, 'Supply category identity IDs do not match the source insert data.', 1;

    -- The original sample uses fixed numeric IDs for all other foreign keys.
    -- Abort instead of mixing with any existing sample or production rows.
    IF EXISTS (SELECT 1 FROM dbo.[users])
        THROW 51100, 'Table users is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[stable_zones])
        THROW 51100, 'Table stable_zones is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[stalls])
        THROW 51100, 'Table stalls is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[horses])
        THROW 51100, 'Table horses is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[horse_health_metrics])
        THROW 51100, 'Table horse_health_metrics is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[supplies])
        THROW 51100, 'Table supplies is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[zone_inventories])
        THROW 51100, 'Table zone_inventories is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[supply_requests])
        THROW 51100, 'Table supply_requests is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[audit_logs])
        THROW 51100, 'Table audit_logs is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[supply_request_items])
        THROW 51100, 'Table supply_request_items is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[training_plans])
        THROW 51100, 'Table training_plans is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[training_phases])
        THROW 51100, 'Table training_phases is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[training_sessions])
        THROW 51100, 'Table training_sessions is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[session_evaluations])
        THROW 51100, 'Table session_evaluations is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[sensor_readings])
        THROW 51100, 'Table sensor_readings is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[performance_alerts])
        THROW 51100, 'Table performance_alerts is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[incident_reports])
        THROW 51100, 'Table incident_reports is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[incident_images])
        THROW 51100, 'Table incident_images is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[diet_plans])
        THROW 51100, 'Table diet_plans is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[diet_plan_items])
        THROW 51100, 'Table diet_plan_items is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[care_tasks])
        THROW 51100, 'Table care_tasks is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[inventory_transactions])
        THROW 51100, 'Table inventory_transactions is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[medical_records])
        THROW 51100, 'Table medical_records is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[treatment_plans])
        THROW 51100, 'Table treatment_plans is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[prescription_items])
        THROW 51100, 'Table prescription_items is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[injuries])
        THROW 51100, 'Table injuries is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[injury_progress_logs])
        THROW 51100, 'Table injury_progress_logs is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[training_locks])
        THROW 51100, 'Table training_locks is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[preventive_care_schedules])
        THROW 51100, 'Table preventive_care_schedules is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[tournaments])
        THROW 51100, 'Table tournaments is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[races])
        THROW 51100, 'Table races is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[race_registrations])
        THROW 51100, 'Table race_registrations is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[race_results])
        THROW 51100, 'Table race_results is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[expenses])
        THROW 51100, 'Table expenses is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[owner_reports])
        THROW 51100, 'Table owner_reports is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;
    IF EXISTS (SELECT 1 FROM dbo.[notifications])
        THROW 51100, 'Table notifications is not empty. Seed refused to avoid duplicates/wrong foreign keys.', 1;

    -- Original sample data (adapted only where target columns differ):

    -- ----------------------------------------------------------
    -- users
    -- ----------------------------------------------------------
    INSERT INTO users (role_id, username, password_hash, full_name, email, status) VALUES
        (5, N'demo_manager', N'$2y$10$hEhXYTLfKcSG.VnWRF8j/.NWXz13ZhOLHLyaos0Kz/9mbMqWLeqgS', N'Nguyễn Minh Quân', N'demo_manager@example.com', N'ACTIVE'),
        (1, N'demo_trainer', N'$2y$10$I6oN4Xr3wRicrN4eufHvQOPxFKeDYifSIhDbVgq8T5AVnsdTYKX1u', N'Trần Quốc Huy', N'demo_trainer@example.com', N'ACTIVE'),
        (2, N'demo_vet', N'$2y$10$VS935T2/1f4O46UUE2KGS.VWOZELE2I9RG1ro.pyjU7TobK/hxPJK', N'Lê Thu Hà', N'demo_vet@example.com', N'ACTIVE'),
        (3, N'demo_groom1', N'$2y$10$pdnxCFOgFIEVKF4DF/Uqfut2wSncdh2IOhuxfHBEyoHvVjyyLgFHu', N'Phạm Văn Nam', N'demo_groom1@example.com', N'ACTIVE'),
        (3, N'demo_groom2', N'$2y$10$6o07ENXlag9j2bCDPgNP0OJoa3.ScZO/O0DLpPaDp9LG/WLG56C5y', N'Võ Ngọc Mai', N'demo_groom2@example.com', N'ACTIVE'),
        (4, N'demo_owner1', N'$2y$10$nNxIsZXsvsF5EJEzZJh9Z.h7zifVQ6sReOXdSgfOBuyBWSRQHY6Ui', N'Đặng Hoàng Anh', N'demo_owner1@example.com', N'ACTIVE'),
        (4, N'demo_owner2', N'$2y$10$SKqBBwiJDRfB6zIYFe0B/eguHnnZJJeSvpVwhk4GfvTULuulkP3BC', N'Bùi Thanh Tâm', N'demo_owner2@example.com', N'ACTIVE'),
        (5, N'demo_manager2', N'$2y$10$iplooReGXKpBoWmrWKCzpeW672Wxc4DvZroLVq82S8gm.boiDkZGe', N'Nguyễn Hải Đăng', N'demo_manager2@example.com', N'ACTIVE'),
        (5, N'demo_manager3', N'$2y$10$kmjd9mqmXr.hktYPbomhNO.kJnahgvVylc6YV3eKxh1yHJK3ykJsC', N'Trần Bảo Ngọc', N'demo_manager3@example.com', N'ACTIVE'),
        (5, N'demo_manager4', N'$2y$10$TUlDD8oK0kz0TchqXBTi5uRwMovgiHcwhGfujPcv7Lmmk3bsQLzAe', N'Lê Minh Khang', N'demo_manager4@example.com', N'ACTIVE'),
        (1, N'demo_trainer2', N'$2y$10$id12MwCbj2HliRcTh97x5uAly5bDna.cMGvz6Iz/5kfXV.a7eofRC', N'Phạm Anh Tuấn', N'demo_trainer2@example.com', N'ACTIVE'),
        (1, N'demo_trainer3', N'$2y$10$taug69e3Z1rPhJGtbkEf1OLi1IfbnXzKAfKi.pG7z6l1MWBjJtZN2', N'Võ Thành Đạt', N'demo_trainer3@example.com', N'ACTIVE'),
        (1, N'demo_trainer4', N'$2y$10$xmkyDZM5pixookNn8FZ9eOi2hjq9W8BaVCG7BMq7oeG0hf9QZ8.gy', N'Đỗ Quang Vinh', N'demo_trainer4@example.com', N'ACTIVE'),
        (2, N'demo_vet2', N'$2y$10$7cMTv64S6QQHrioNo20gDeRRmrnIZXJbIAqTgtGvHYzAPkZSsylTO', N'Nguyễn Thảo Linh', N'demo_vet2@example.com', N'ACTIVE'),
        (2, N'demo_vet3', N'$2y$10$EkCzfbfQgdfqTgqInlgWXekH6lu8671PqytJ3j6u3ZtPs9rVS6Ka.', N'Trần Ngọc Hân', N'demo_vet3@example.com', N'ACTIVE'),
        (2, N'demo_vet4', N'$2y$10$ghxjca8R8K.a7ncGkG1us.y1Tny1ugEzhxaaXhHNmwjWkF98907UW', N'Phan Hoàng Phúc', N'demo_vet4@example.com', N'ACTIVE'),
        (3, N'demo_groom3', N'$2y$10$yUxj5AN3Hr0zr3TTnYS9h.rGrBc5JWvkyxNrv/eqBoeSilSLNBAkG', N'Lê Văn Bình', N'demo_groom3@example.com', N'ACTIVE'),
        (3, N'demo_groom4', N'$2y$10$sjcrzXgNsu0qrfH83/QLB.mO6polwbSECS.KcIR9l67NLPg/NJyOC', N'Nguyễn Mỹ Duyên', N'demo_groom4@example.com', N'ACTIVE'),
        (4, N'demo_owner3', N'$2y$10$ezlOUn7XsXBek7iqH6mQqug2cDxz42HYd..b.f23hJDbazT61GjCG', N'Trần Gia Bảo', N'demo_owner3@example.com', N'ACTIVE'),
        (4, N'demo_owner4', N'$2y$10$QtsZgHkm0Gkc1MRAuSNKvORICXroXNtCelflxjV3se35CgFjJv2gS', N'Lý Khánh An', N'demo_owner4@example.com', N'ACTIVE');

    -- stable_zones

    -- ----------------------------------------------------------
    -- stable_zones
    -- ----------------------------------------------------------
    INSERT INTO stable_zones (zone_code, zone_name, description, responsible_user_id) VALUES
        (N'DEMO-A', N'Khu huấn luyện A', N'Khu ngựa đang tập luyện', 4),
        (N'DEMO-B', N'Khu phục hồi B', N'Khu theo dõi và phục hồi', 5),
        (N'DEMO-C', N'Khu huấn luyện C', N'Khu ngựa đang tập luyện', 17),
        (N'DEMO-D', N'Khu huấn luyện D', N'Khu ngựa đang tập luyện', 18);

    -- stalls

    -- ----------------------------------------------------------
    -- stalls
    -- ----------------------------------------------------------
    INSERT INTO stalls (zone_id, stall_code, stall_type, status) VALUES
        (1, N'DEMO-A01', N'STANDARD', N'OCCUPIED'),
        (1, N'DEMO-A02', N'STANDARD', N'OCCUPIED'),
        (2, N'DEMO-B01', N'RECOVERY', N'OCCUPIED'),
        (1, N'DEMO-A03', N'STANDARD', N'AVAILABLE'),
        (2, N'DEMO-B02', N'QUARANTINE', N'AVAILABLE'),
        (3, N'DEMO-C01', N'STANDARD', N'OCCUPIED'),
        (3, N'DEMO-C02', N'STANDARD', N'OCCUPIED'),
        (4, N'DEMO-D01', N'STANDARD', N'OCCUPIED'),
        (4, N'DEMO-D02', N'STANDARD', N'OCCUPIED');

    -- horses

    -- ----------------------------------------------------------
    -- horses
    -- ----------------------------------------------------------
    INSERT INTO horses (owner_id, manager_id, stall_id, name, registration_no, microchip_no, breed, gender, color, date_of_birth, country_of_origin, height_cm, current_weight_kg, health_status, readiness_status, is_training_locked) VALUES
        (6, 2, 1, N'Thiên Phong', N'DEMO-REG-001', N'DEMO-CHIP-001', N'Thoroughbred', N'STALLION', N'Nâu', N'20210315', N'Việt Nam', 162, 510, N'ELIGIBLE', N'READY', 0),
        (6, 2, 2, N'Bạch Vân', N'DEMO-REG-002', N'DEMO-CHIP-002', N'Thoroughbred', N'MARE', N'Xám', N'20220420', N'Việt Nam', 162, 475, N'ELIGIBLE', N'READY', 0),
        (7, 2, 3, N'Hồng Lôi', N'DEMO-REG-003', N'DEMO-CHIP-003', N'Thoroughbred', N'GELDING', N'Hạt dẻ', N'20200210', N'Việt Nam', 162, 495, N'INJURED', N'RESTING', 1),
        (7, 11, 6, N'Hắc Long', N'DEMO-REG-004', N'DEMO-CHIP-004', N'Thoroughbred', N'STALLION', N'Đen', N'20210512', N'Việt Nam', 162, 520, N'ELIGIBLE', N'READY', 0),
        (19, 12, 7, N'Kim Tinh', N'DEMO-REG-005', N'DEMO-CHIP-005', N'Thoroughbred', N'MARE', N'Vàng', N'20220308', N'Việt Nam', 162, 480, N'ELIGIBLE', N'READY', 0),
        (19, 12, 8, N'Ngân Hà', N'DEMO-REG-006', N'DEMO-CHIP-006', N'Thoroughbred', N'MARE', N'Xám', N'20210719', N'Việt Nam', 162, 490, N'ELIGIBLE', N'READY', 0),
        (20, 13, 9, N'Bão Táp', N'DEMO-REG-007', N'DEMO-CHIP-007', N'Thoroughbred', N'GELDING', N'Nâu', N'20200425', N'Việt Nam', 162, 505, N'ELIGIBLE', N'READY', 0);

    -- horse_health_metrics

    -- ----------------------------------------------------------
    -- horse_health_metrics
    -- ----------------------------------------------------------
    INSERT INTO horse_health_metrics (horse_id, recorded_by, recorded_at, weight_kg, resting_heart_rate, body_temperature, respiratory_rate, notes) VALUES
        (1, 3, N'2026-10-01T06:00:00', 508, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (1, 3, N'2026-10-08T06:00:00', 510, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (2, 3, N'2026-10-01T06:00:00', 473, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (2, 3, N'2026-10-08T06:00:00', 475, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (3, 3, N'2026-10-01T06:00:00', 493, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (3, 3, N'2026-10-08T06:00:00', 495, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (4, 14, N'2026-10-01T06:00:00', 518, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (4, 14, N'2026-10-08T06:00:00', 520, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (5, 15, N'2026-10-01T06:00:00', 478, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (5, 15, N'2026-10-08T06:00:00', 480, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (6, 15, N'2026-10-01T06:00:00', 488, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (6, 15, N'2026-10-08T06:00:00', 490, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (7, 16, N'2026-10-01T06:00:00', 503, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử'),
        (7, 16, N'2026-10-08T06:00:00', 505, 36, 37.5, 12, N'Dữ liệu đo mẫu phục vụ kiểm thử');

    -- supply_categories

    -- ----------------------------------------------------------
    -- supplies
    -- ----------------------------------------------------------
    INSERT INTO supplies (category_id, supply_code, supply_name, unit, unit_price, min_stock_level, description) VALUES
        (1, N'DEMO-OATS', N'Yến mạch', N'kg', 25000, 50, N'Vật tư minh họa cho dữ liệu demo'),
        (2, N'DEMO-HAY', N'Cỏ khô Timothy', N'kg', 15000, 100, N'Vật tư minh họa cho dữ liệu demo'),
        (3, N'DEMO-VIT', N'Vitamin tổng hợp mẫu', N'hộp', 250000, 5, N'Vật tư minh họa cho dữ liệu demo'),
        (4, N'DEMO-MED', N'Dung dịch chăm sóc mẫu', N'chai', 120000, 5, N'Vật tư minh họa cho dữ liệu demo'),
        (5, N'DEMO-VAC', N'Vắc-xin mẫu', N'lọ', 350000, 5, N'Vật tư minh họa cho dữ liệu demo'),
        (6, N'DEMO-BRUSH', N'Bàn chải chăm sóc', N'cái', 80000, 3, N'Vật tư minh họa cho dữ liệu demo');

    -- zone_inventories

    -- ----------------------------------------------------------
    -- zone_inventories
    -- ----------------------------------------------------------
    INSERT INTO zone_inventories (zone_id, supply_id, quantity, updated_at) VALUES
        (1, 1, 196, N'2026-10-08T06:35:00'),
        (1, 2, 500, N'2026-10-08T06:35:00'),
        (1, 3, 20, N'2026-10-08T06:35:00'),
        (1, 6, 10, N'2026-10-08T06:35:00'),
        (2, 2, 200, N'2026-10-08T06:35:00'),
        (2, 4, 20, N'2026-10-08T06:35:00'),
        (2, 5, 10, N'2026-10-08T06:35:00'),
        (3, 1, 196, N'2026-10-08T06:35:00'),
        (3, 2, 500, N'2026-10-08T06:35:00'),
        (4, 1, 196, N'2026-10-08T06:35:00'),
        (4, 2, 500, N'2026-10-08T06:35:00');

    -- supply_requests

    -- ----------------------------------------------------------
    -- supply_requests
    -- ----------------------------------------------------------
    INSERT INTO supply_requests (zone_id, requested_by, approved_by, status, note, requested_at, processed_at) VALUES
        (1, 4, 1, N'FULFILLED', N'Nhập vật tư đầu kỳ', N'2026-09-30T08:00:00', N'2026-10-01T07:00:00'),
        (2, 5, NULL, N'PENDING', N'Bổ sung vật tư khu phục hồi', N'2026-10-08T09:00:00', NULL),
        (1, 4, 1, N'REJECTED', N'Chưa cần bổ sung dụng cụ', N'2026-10-07T08:00:00', N'2026-10-07T10:00:00');

    -- audit_logs

    -- ----------------------------------------------------------
    -- audit_logs
    -- ----------------------------------------------------------
    INSERT INTO audit_logs (user_id, action, entity_name, entity_id, new_value, ip_address, user_agent, created_at, old_value) VALUES
        (1, N'CREATE', N'horses', N'1', N'{"name":"Thiên Phong","registration_no":"DEMO-REG-001"}', N'127.0.0.1', N'Demo seed script', N'2026-09-01T08:00:00', NULL),
        (3, N'UPDATE', N'horses', N'3', N'{"health_status":"INJURED","is_training_locked":true}', N'127.0.0.1', N'Demo seed script', N'2026-10-03T08:30:00', N'{"health_status":"ELIGIBLE","is_training_locked":false}'),
        (1, N'APPROVE', N'supply_requests', N'1', N'{"status":"APPROVED"}', N'127.0.0.1', N'Demo seed script', N'2026-09-30T10:00:00', N'{"status":"PENDING"}');

    -- supply_request_items

    -- ----------------------------------------------------------
    -- supply_request_items
    -- ----------------------------------------------------------
    INSERT INTO supply_request_items (request_id, supply_id, quantity) VALUES
        (1, 1, 200),
        (1, 2, 500),
        (2, 4, 10),
        (2, 2, 100),
        (3, 6, 5);

    -- training_plans

    -- ----------------------------------------------------------
    -- training_plans
    -- ----------------------------------------------------------
    INSERT INTO training_plans (horse_id, trainer_id, plan_name, goal, start_date, end_date, status) VALUES
        (1, 2, N'DEMO - Giáo án tháng 10 - Thiên Phong', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (2, 2, N'DEMO - Giáo án tháng 10 - Bạch Vân', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (3, 2, N'DEMO - Giáo án tháng 10 - Hồng Lôi', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (4, 11, N'DEMO - Giáo án tháng 10 - Hắc Long', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (5, 12, N'DEMO - Giáo án tháng 10 - Kim Tinh', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (6, 12, N'DEMO - Giáo án tháng 10 - Ngân Hà', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE'),
        (7, 13, N'DEMO - Giáo án tháng 10 - Bão Táp', N'Duy trì thể lực và theo dõi khả năng phục hồi', N'20261001', N'20261031', N'ACTIVE');

    -- training_phases

    -- ----------------------------------------------------------
    -- training_phases
    -- ----------------------------------------------------------
    INSERT INTO training_phases (plan_id, phase_order, phase_name, phase_type, start_date, end_date, target_distance_m, target_volume, track_surface) VALUES
        (1, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (1, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (2, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (2, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (3, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (3, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (4, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (4, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (5, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (5, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (6, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (6, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF'),
        (7, 1, N'Nền tảng thể lực', N'BASE', N'20261001', N'20261015', 1200, N'3 buổi/tuần', N'DIRT'),
        (7, 2, N'Tăng sức bền', N'BUILD', N'20261016', N'20261031', 1600, N'3 buổi/tuần', N'TURF');

    -- training_sessions

    -- ----------------------------------------------------------
    -- training_sessions
    -- ----------------------------------------------------------
    INSERT INTO training_sessions (horse_id, phase_id, assigned_staff_id, session_date, start_time, end_time, session_type, intensity, planned_distance_m, track_surface, status, instructions) VALUES
        (1, 1, 2, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (1, 1, 2, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt'),
        (2, 3, 2, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (2, 3, 2, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt'),
        (3, 5, 2, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (3, 5, 2, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'BLOCKED', N'Tạm khóa theo yêu cầu bác sĩ thú y'),
        (4, 7, 11, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (4, 7, 11, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt'),
        (5, 9, 12, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (5, 9, 12, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt'),
        (6, 11, 12, N'20261002', N'07:30:00', N'08:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (6, 11, 12, N'20261010', N'07:30:00', N'08:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt'),
        (7, 13, 13, N'20261002', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'COMPLETED', N'Khởi động trước khi tập'),
        (7, 13, 13, N'20261010', N'06:30:00', N'07:00:00', N'WORKOUT', N'MODERATE', 1200, N'DIRT', N'SCHEDULED', N'Theo giáo án đã duyệt');

    -- session_evaluations

    -- ----------------------------------------------------------
    -- session_evaluations
    -- ----------------------------------------------------------
    INSERT INTO session_evaluations (session_id, evaluator_id, actual_distance_m, duration_sec, avg_speed_kmh, max_speed_kmh, avg_heart_rate, max_heart_rate, recovery_heart_rate, performance_rating, comment, evaluated_at) VALUES
        (1, 2, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00'),
        (3, 2, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00'),
        (5, 2, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00'),
        (7, 11, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00'),
        (9, 12, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00'),
        (11, 12, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T08:10:00'),
        (13, 13, 1200, 120, 36, 42, 140, 165, 60, 8, N'Hoàn thành cự ly mục tiêu', N'2026-10-02T07:10:00');

    -- sensor_readings

    -- ----------------------------------------------------------
    -- sensor_readings
    -- ----------------------------------------------------------
    INSERT INTO sensor_readings (session_id, recorded_at, heart_rate, speed_kmh, distance_m) VALUES
        (1, N'2026-10-02T06:31:00', 140, 36, 600),
        (1, N'2026-10-02T06:31:30', 150, 36, 900),
        (1, N'2026-10-02T06:32:00', 160, 36, 1200),
        (3, N'2026-10-02T06:31:00', 140, 36, 600),
        (3, N'2026-10-02T06:31:30', 150, 36, 900),
        (3, N'2026-10-02T06:32:00', 160, 36, 1200),
        (5, N'2026-10-02T06:31:00', 140, 36, 600),
        (5, N'2026-10-02T06:31:30', 150, 36, 900),
        (5, N'2026-10-02T06:32:00', 160, 36, 1200),
        (7, N'2026-10-02T06:31:00', 140, 36, 600),
        (7, N'2026-10-02T06:31:30', 150, 36, 900),
        (7, N'2026-10-02T06:32:00', 160, 36, 1200),
        (9, N'2026-10-02T06:31:00', 140, 36, 600),
        (9, N'2026-10-02T06:31:30', 150, 36, 900),
        (9, N'2026-10-02T06:32:00', 160, 36, 1200),
        (11, N'2026-10-02T07:31:00', 140, 36, 600),
        (11, N'2026-10-02T07:31:30', 150, 36, 900),
        (11, N'2026-10-02T07:32:00', 160, 36, 1200),
        (13, N'2026-10-02T06:31:00', 140, 36, 600),
        (13, N'2026-10-02T06:31:30', 150, 36, 900),
        (13, N'2026-10-02T06:32:00', 160, 36, 1200);

    -- performance_alerts

    -- ----------------------------------------------------------
    -- performance_alerts
    -- ----------------------------------------------------------
    INSERT INTO performance_alerts (horse_id, session_id, alert_type, severity, message, is_acknowledged, acknowledged_by, acknowledged_at, created_at) VALUES
        (3, 5, N'FATIGUE', N'MEDIUM', N'Cảnh báo mệt mỏi mẫu, cần theo dõi sau buổi tập', 1, 2, N'2026-10-02T07:15:00', N'2026-10-02T07:00:00');

    -- incident_reports

    -- ----------------------------------------------------------
    -- incident_reports
    -- ----------------------------------------------------------
    INSERT INTO incident_reports (horse_id, reported_by, incident_type, severity, description, status, reported_at, resolved_at) VALUES
        (3, 5, N'LAMENESS', N'MEDIUM', N'Dữ liệu mẫu: đi khập khiễng sau vận động', N'IN_REVIEW', N'2026-10-03T07:00:00', NULL),
        (2, 4, N'HOOF_SCRATCH', N'LOW', N'Dữ liệu mẫu: vết trầy vùng móng', N'RESOLVED', N'2026-09-10T07:00:00', N'2026-09-15T10:00:00');

    -- incident_images

    -- ----------------------------------------------------------
    -- incident_images
    -- ----------------------------------------------------------
    INSERT INTO incident_images (incident_id, image_url, uploaded_at) VALUES
        (1, N'https://example.com/demo/incidents/incident-1.jpg', N'2026-10-03T07:05:00'),
        (2, N'https://example.com/demo/incidents/incident-2.jpg', N'2026-09-10T07:05:00');

    -- diet_plans

    -- ----------------------------------------------------------
    -- diet_plans
    -- ----------------------------------------------------------
    INSERT INTO diet_plans (horse_id, created_by, approved_by, effective_from, effective_to, status, notes, created_at, approved_at) VALUES
        (1, 2, 3, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (2, 2, 3, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (3, 2, 3, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (4, 11, 14, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (5, 12, 15, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (6, 12, 15, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00'),
        (7, 13, 16, N'20261001', N'20261031', N'APPROVED', N'Khẩu phần minh họa', N'2026-09-30T08:00:00', N'2026-09-30T10:00:00');

    -- diet_plan_items

    -- ----------------------------------------------------------
    -- diet_plan_items
    -- ----------------------------------------------------------
    INSERT INTO diet_plan_items (diet_plan_id, supply_id, meal_type, meal_time, quantity, unit, note) VALUES
        (1, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (1, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (1, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (2, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (2, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (2, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (3, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (3, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (3, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (4, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (4, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (4, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (5, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (5, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (5, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (6, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (6, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (6, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập'),
        (7, 1, N'BREAKFAST', N'06:00:00', 2, N'kg', N'Số liệu giả lập'),
        (7, 2, N'LUNCH', N'12:00:00', 4, N'kg', N'Số liệu giả lập'),
        (7, 2, N'DINNER', N'18:00:00', 4, N'kg', N'Số liệu giả lập');

    -- care_tasks

    -- ----------------------------------------------------------
    -- care_tasks
    -- ----------------------------------------------------------
    INSERT INTO care_tasks (horse_id, assigned_to, completed_by, task_type, scheduled_at, status, completed_at, note) VALUES
        (1, 4, 4, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (1, 4, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (2, 4, 4, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (2, 4, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (3, 5, 5, N'GROOMING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Hoàn thành chải lông'),
        (3, 5, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (4, 17, 17, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (4, 17, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (5, 17, 17, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (5, 17, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (6, 18, 18, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (6, 18, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL),
        (7, 18, 18, N'FEEDING', N'2026-10-08T06:00:00', N'DONE', N'2026-10-08T06:30:00', N'Cho ăn 2 kg yến mạch'),
        (7, 18, NULL, N'STALL_CLEANING', N'2026-10-10T08:00:00', N'PENDING', NULL, NULL);

    -- inventory_transactions

    -- ----------------------------------------------------------
    -- inventory_transactions
    -- ----------------------------------------------------------
    INSERT INTO inventory_transactions (zone_id, supply_id, performed_by, transaction_type, quantity, balance_after, reference_type, reference_id, notes, transaction_date) VALUES
        (1, 1, 1, N'IMPORT', 200, 200, N'SUPPLY_REQUEST', 1, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (1, 2, 1, N'IMPORT', 500, 500, N'SUPPLY_REQUEST', 1, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (1, 3, 1, N'IMPORT', 20, 20, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (1, 6, 1, N'IMPORT', 10, 10, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (2, 2, 1, N'IMPORT', 200, 200, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (2, 4, 1, N'IMPORT', 20, 20, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (2, 5, 1, N'IMPORT', 10, 10, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (3, 1, 8, N'IMPORT', 200, 200, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (3, 2, 8, N'IMPORT', 500, 500, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (4, 1, 10, N'IMPORT', 200, 200, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (4, 2, 10, N'IMPORT', 500, 500, NULL, NULL, N'Nhập tồn kho đầu kỳ mẫu', N'2026-10-01T07:00:00'),
        (1, 1, 4, N'CONSUMPTION', 2, 198, N'CARE_TASK', 1, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:30:00'),
        (1, 1, 4, N'CONSUMPTION', 2, 196, N'CARE_TASK', 3, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:35:00'),
        (3, 1, 17, N'CONSUMPTION', 2, 198, N'CARE_TASK', 7, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:30:00'),
        (3, 1, 17, N'CONSUMPTION', 2, 196, N'CARE_TASK', 9, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:35:00'),
        (4, 1, 18, N'CONSUMPTION', 2, 198, N'CARE_TASK', 11, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:30:00'),
        (4, 1, 18, N'CONSUMPTION', 2, 196, N'CARE_TASK', 13, N'Xuất yến mạch cho bữa sáng', N'2026-10-08T06:35:00');

    -- medical_records

    -- ----------------------------------------------------------
    -- medical_records
    -- ----------------------------------------------------------
    INSERT INTO medical_records (horse_id, vet_id, incident_id, exam_date, reason, symptoms, diagnosis, health_status_after, notes) VALUES
        (3, 3, 1, N'2026-10-03T08:00:00', N'Kiểm tra sau báo cáo sự cố', N'Dáng đi không đều', N'Chấn thương phần mềm mẫu', N'INJURED', N'Nội dung minh họa, không phải chỉ định điều trị'),
        (2, 3, 2, N'2026-09-10T08:00:00', N'Kiểm tra sau báo cáo sự cố', N'Trầy vùng móng', N'Trầy nhẹ đã phục hồi', N'ELIGIBLE', N'Nội dung minh họa, không phải chỉ định điều trị');

    -- treatment_plans

    -- ----------------------------------------------------------
    -- treatment_plans
    -- ----------------------------------------------------------
    INSERT INTO treatment_plans (record_id, description, start_date, end_date, status) VALUES
        (1, N'Phác đồ mẫu: theo dõi và tái khám theo chỉ định bác sĩ', N'20261003', N'20261017', N'ACTIVE'),
        (2, N'Phác đồ mẫu: theo dõi và tái khám theo chỉ định bác sĩ', N'20260910', N'20260915', N'COMPLETED');

    -- prescription_items

    -- ----------------------------------------------------------
    -- prescription_items
    -- ----------------------------------------------------------
    INSERT INTO prescription_items (treatment_id, supply_id, dosage, frequency, duration_days, route, instructions) VALUES
        (1, 4, N'Liều mẫu do bác sĩ thiết lập', N'Theo chỉ định trong hồ sơ', 14, N'TOPICAL', N'Dữ liệu giả lập để kiểm thử giao diện'),
        (2, 4, N'Liều mẫu do bác sĩ thiết lập', N'Theo chỉ định trong hồ sơ', 5, N'TOPICAL', N'Dữ liệu giả lập để kiểm thử giao diện');

    -- injuries

    -- ----------------------------------------------------------
    -- injuries
    -- ----------------------------------------------------------
    INSERT INTO injuries (record_id, body_part, body_system, model_mesh_id, position_x, position_y, position_z, injury_type, severity, status, occurred_date, healed_date, description) VALUES
        (1, N'Chân trước trái', N'MUSCLE', N'front_left_leg', 0.2, 0.5, 0.1, N'Chấn thương phần mềm', N'MODERATE', N'RECOVERING', N'20261003', NULL, N'Chấn thương minh họa'),
        (2, N'Móng chân sau phải', N'HOOF', N'rear_right_hoof', 0.2, 0.5, 0.1, N'Trầy xước', N'MINOR', N'HEALED', N'20260910', N'20260915', N'Chấn thương minh họa');

    -- injury_progress_logs

    -- ----------------------------------------------------------
    -- injury_progress_logs
    -- ----------------------------------------------------------
    INSERT INTO injury_progress_logs (injury_id, logged_by, log_date, recovery_percent, pain_level, notes) VALUES
        (1, 3, N'2026-10-03T09:00:00', 10, 5, N'Cập nhật hồi phục mẫu'),
        (1, 3, N'2026-10-08T09:00:00', 50, 2, N'Cập nhật hồi phục mẫu'),
        (2, 3, N'2026-09-15T09:00:00', 100, 0, N'Cập nhật hồi phục mẫu');

    -- training_locks

    -- ----------------------------------------------------------
    -- training_locks
    -- ----------------------------------------------------------
    INSERT INTO training_locks (horse_id, locked_by, injury_id, lock_level, reason, locked_at, expected_end_at, released_at, released_by, status) VALUES
        (3, 3, 1, N'FULL', N'Tạm nghỉ để theo dõi hồi phục', N'2026-10-03T08:30:00', N'2026-10-17T08:30:00', NULL, NULL, N'ACTIVE'),
        (2, 3, 2, N'HEAVY_ONLY', N'Tạm nghỉ để theo dõi hồi phục', N'2026-09-10T08:30:00', N'2026-09-15T10:00:00', N'2026-09-15T10:00:00', 3, N'RELEASED');

    -- preventive_care_schedules

    -- ----------------------------------------------------------
    -- preventive_care_schedules
    -- ----------------------------------------------------------
    INSERT INTO preventive_care_schedules (horse_id, care_type, description, due_date, interval_days, remind_before_days, status, completed_date, performed_by) VALUES
        (1, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (1, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 3),
        (2, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (2, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 3),
        (3, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (3, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 3),
        (4, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (4, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 14),
        (5, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (5, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 15),
        (6, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (6, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 15),
        (7, N'VACCINATION', N'Lịch tiêm phòng mẫu', N'20261015', 180, 7, N'PENDING', NULL, NULL),
        (7, N'FARRIER', N'Kiểm tra móng định kỳ', N'20260920', 42, 3, N'DONE', N'20260920', 16);

    -- tournaments

    -- ----------------------------------------------------------
    -- tournaments
    -- ----------------------------------------------------------
    INSERT INTO tournaments (name, organizer, location, start_date, end_date, status) VALUES
        (N'DEMO - Giải đua mùa thu 2026', N'Câu lạc bộ ngựa demo', N'Trường đua mẫu', N'20260925', N'20260927', N'FINISHED'),
        (N'DEMO - Giải đua tháng 11', N'Câu lạc bộ ngựa demo', N'Trường đua mẫu', N'20261115', N'20261116', N'UPCOMING');

    -- races

    -- ----------------------------------------------------------
    -- races
    -- ----------------------------------------------------------
    INSERT INTO races (tournament_id, race_name, race_datetime, distance_m, track_surface, race_class, min_age, max_age, prize_pool, entry_fee, max_runners, registration_deadline, status) VALUES
        (1, N'DEMO - Cúp mùa thu 1200 m', N'2026-09-26T09:00:00', 1200, N'TURF', N'Open', 3, 8, 100000000, 2000000, 12, N'2026-09-20T17:00:00', N'FINISHED'),
        (2, N'DEMO - Cúp tháng 11 1600 m', N'2026-11-15T09:00:00', 1600, N'TURF', N'Open', 3, 8, 100000000, 2000000, 12, N'2026-11-01T17:00:00', N'OPEN');

    -- race_registrations

    -- ----------------------------------------------------------
    -- race_registrations
    -- ----------------------------------------------------------
    INSERT INTO race_registrations (race_id, horse_id, registered_by, jockey_name, gate_number, status, registered_at) VALUES
        (1, 1, 2, N'Nguyễn Văn Sơn', 1, N'APPROVED', N'2026-09-18T08:00:00'),
        (2, 1, 2, N'Nguyễn Văn Sơn', NULL, N'PENDING', N'2026-10-08T08:00:00'),
        (1, 2, 2, N'Trần Minh Đức', 2, N'APPROVED', N'2026-09-18T08:00:00'),
        (2, 2, 2, N'Trần Minh Đức', NULL, N'PENDING', N'2026-10-08T08:00:00');

    -- race_results

    -- ----------------------------------------------------------
    -- race_results
    -- ----------------------------------------------------------
    INSERT INTO race_results (registration_id, finish_position, finish_time_sec, is_dnf, prize_money, notes, recorded_at) VALUES
        (1, 1, 72.35, 0, 60000000, N'Kết quả minh họa', N'2026-09-26T10:00:00'),
        (3, 2, 73.12, 0, 40000000, N'Kết quả minh họa', N'2026-09-26T10:00:00');

    -- expenses

    -- ----------------------------------------------------------
    -- expenses
    -- ----------------------------------------------------------
    INSERT INTO expenses (horse_id, recorded_by, category, amount, expense_date, description) VALUES
        (1, 1, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (1, 1, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (1, 1, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (1, 1, N'RACE_FEE', 2000000, N'20260918', N'Phí tham dự cúp mùa thu'),
        (2, 1, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (2, 1, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (2, 1, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (2, 1, N'RACE_FEE', 2000000, N'20260918', N'Phí tham dự cúp mùa thu'),
        (2, 1, N'MEDICAL', 500000, N'20260910', N'Chi phí chăm sóc vết trầy mẫu'),
        (3, 1, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (3, 1, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (3, 1, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (4, 8, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (4, 8, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (4, 8, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (5, 9, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (5, 9, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (5, 9, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (6, 9, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (6, 9, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (6, 9, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (7, 10, N'FEED', 1500000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (7, 10, N'TRAINING', 3000000, N'20260930', N'Chi phí tháng 9 mẫu'),
        (7, 10, N'STABLE', 2000000, N'20260930', N'Chi phí tháng 9 mẫu');

    -- owner_reports

    -- ----------------------------------------------------------
    -- owner_reports
    -- ----------------------------------------------------------
    INSERT INTO owner_reports (owner_id, horse_id, period_start, period_end, total_feed_cost, total_medical_cost, total_other_cost, total_prize_revenue, generated_at) VALUES
        (6, 1, N'20260901', N'20260930', 1500000, 0, 7000000, 60000000, N'2026-10-01T08:00:00'),
        (6, 2, N'20260901', N'20260930', 1500000, 500000, 7000000, 40000000, N'2026-10-01T08:00:00'),
        (7, 3, N'20260901', N'20260930', 1500000, 0, 5000000, 0, N'2026-10-01T08:00:00'),
        (7, 4, N'20260901', N'20260930', 1500000, 0, 5000000, 0, N'2026-10-01T08:00:00'),
        (19, 5, N'20260901', N'20260930', 1500000, 0, 5000000, 0, N'2026-10-01T08:00:00'),
        (19, 6, N'20260901', N'20260930', 1500000, 0, 5000000, 0, N'2026-10-01T08:00:00'),
        (20, 7, N'20260901', N'20260930', 1500000, 0, 5000000, 0, N'2026-10-01T08:00:00');

    -- notifications

    -- ----------------------------------------------------------
    -- notifications
    -- ----------------------------------------------------------
    INSERT INTO notifications (user_id, type, title, content, reference_type, reference_id, is_read, created_at) VALUES
        (3, N'INCIDENT', N'Sự cố cần theo dõi', N'Hồng Lôi đang được theo dõi hồi phục', N'incident', 1, 0, N'2026-10-08T10:00:00'),
        (2, N'TRAINING_LOCK', N'Tạm khóa huấn luyện', N'Hồng Lôi tạm ngừng huấn luyện đến khi bác sĩ cho phép', N'training_lock', 1, 0, N'2026-10-08T10:00:00'),
        (2, N'PERFORMANCE_ALERT', N'Cảnh báo sau buổi tập', N'Đã ghi nhận cảnh báo mệt mỏi', N'performance_alert', 1, 0, N'2026-10-08T10:00:00'),
        (1, N'SUPPLY_REQUEST', N'Yêu cầu vật tư mới', N'Khu phục hồi cần bổ sung vật tư', N'supply_request', 2, 0, N'2026-10-08T10:00:00'),
        (6, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí và thành tích của Thiên Phong', N'owner_report', 1, 0, N'2026-10-08T10:00:00'),
        (7, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí của Hồng Lôi', N'owner_report', 3, 0, N'2026-10-08T10:00:00'),
        (2, N'RACE', N'Giải đua tháng 11', N'Đăng ký đang chờ phê duyệt', N'race', 2, 0, N'2026-10-08T10:00:00'),
        (7, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí của Hắc Long', N'owner_report', 4, 0, N'2026-10-08T10:00:00'),
        (19, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí của Kim Tinh', N'owner_report', 5, 0, N'2026-10-08T10:00:00'),
        (19, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí của Ngân Hà', N'owner_report', 6, 0, N'2026-10-08T10:00:00'),
        (20, N'REPORT', N'Báo cáo tháng 9', N'Đã có báo cáo chi phí của Bão Táp', N'owner_report', 7, 0, N'2026-10-08T10:00:00');

    COMMIT TRANSACTION;
    PRINT 'SUCCESS: Team sample seed inserted into the target horse_management schema.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Read-only verification
SELECT 'users' AS table_name, COUNT(*) AS row_count FROM dbo.users
UNION ALL SELECT 'horses',COUNT(*) FROM dbo.horses
UNION ALL SELECT 'horse_health_metrics',COUNT(*) FROM dbo.horse_health_metrics
UNION ALL SELECT 'medical_records',COUNT(*) FROM dbo.medical_records
UNION ALL SELECT 'injuries',COUNT(*) FROM dbo.injuries
UNION ALL SELECT 'training_locks',COUNT(*) FROM dbo.training_locks
UNION ALL SELECT 'preventive_care_schedules',COUNT(*) FROM dbo.preventive_care_schedules;
GO
