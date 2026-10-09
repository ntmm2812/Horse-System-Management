-- =====================================================================
--  Racehorse Training & Management System
--  Database schema - SQL Server
--  Generated from: doc/ERD_Conceptual.md
--  Rule: only 1-1 and 1-n relationships (n-n resolved by associative tables)
--        1-1 is enforced by a UNIQUE foreign key.
-- =====================================================================

-- ============================================================
-- SQL SERVER CONVERSION NOTES
-- - All 40 tables and relationships from the supplied MySQL schema are preserved.
-- - AUTO_INCREMENT -> IDENTITY(1,1)
-- - ENUM -> NVARCHAR + CHECK constraints
-- - BOOLEAN -> BIT
-- - UNSIGNED integer types -> SQL Server signed integer types
-- - JSON -> NVARCHAR(MAX) + ISJSON checks for audit JSON fields
-- - MySQL ON UPDATE CURRENT_TIMESTAMP removed; update updated_at from Spring Boot
-- - ON DELETE CASCADE/SET NULL removed to avoid SQL Server "multiple cascade paths";
--   foreign keys remain and use SQL Server NO ACTION delete behavior.
-- - The script does NOT drop an existing database.
-- ============================================================

IF DB_ID(N'horse_management') IS NULL
BEGIN
    CREATE DATABASE horse_management;
END
GO

USE horse_management;
GO

-- =====================================================================
-- 1. RBAC & SYSTEM
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : roles - Vai trò người dùng
-- Mục đích: Danh mục 5 vai trò của hệ thống: Head Trainer, Veterinarian, Groom, Horse Owner, Club Manager.
-- Actor  : Club Manager
-- Quan hệ: Role 1-n User ; Role 1-n RolePermission
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    role_id         INT    NOT NULL IDENTITY(1,1),
    role_code       NVARCHAR(50)     NOT NULL,
    role_name       NVARCHAR(100)    NOT NULL,
    description     NVARCHAR(255)    NULL,
    created_at      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_code UNIQUE (role_code)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : permissions - Quyền chức năng
-- Mục đích: Danh mục quyền thao tác theo từng module (HORSE, TRAINING, MEDICAL, CARE, SUPPLY, RACE, ...).
-- Actor  : Club Manager
-- Quan hệ: Permission 1-n RolePermission
-- ---------------------------------------------------------------------
CREATE TABLE permissions (
    permission_id   INT    NOT NULL IDENTITY(1,1),
    permission_code NVARCHAR(100)    NOT NULL,
    module          NVARCHAR(50)     NOT NULL,
    description     NVARCHAR(255)    NULL,
    PRIMARY KEY (permission_id),
    CONSTRAINT uq_permissions_code UNIQUE (permission_code)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : role_permissions - Phân quyền vai trò (RBAC)
-- Mục đích: Bảng trung gian gán quyền cho vai trò (tách quan hệ n-n Role - Permission).
-- Actor  : Club Manager
-- Quan hệ: Role 1-n RolePermission n-1 Permission
-- ---------------------------------------------------------------------
CREATE TABLE role_permissions (
    role_permission_id INT NOT NULL IDENTITY(1,1),
    role_id            INT NOT NULL,
    permission_id      INT NOT NULL,
    granted_at         DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (role_permission_id),
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles (role_id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (permission_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : users - Tài khoản người dùng
-- Mục đích: Tài khoản đăng nhập của nhân sự (HLV, bác sĩ thú y, nhân viên chuồng) và chủ ngựa.
-- Actor  : Tất cả
-- Quan hệ: Role 1-n User ; User 1-n Horse (sở hữu) ; User 1-n Horse (quản lý) ; ...
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id         INT    NOT NULL IDENTITY(1,1),
    role_id         INT    NOT NULL,
    username        NVARCHAR(50)     NOT NULL,
    password_hash   NVARCHAR(255)    NOT NULL,
    full_name       NVARCHAR(100)    NOT NULL,
    email           NVARCHAR(150)    NOT NULL,
    phone           NVARCHAR(20)     NULL,
    avatar_url      NVARCHAR(500)    NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    last_login_at   DATETIME2        NULL,
    created_at      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    updated_at      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : audit_logs - Nhật ký thao tác hệ thống
-- Mục đích: Ghi lại mọi thao tác thêm/sửa/xóa/đăng nhập (giá trị cũ & mới) để đảm bảo minh bạch.
-- Actor  : Club Manager
-- Quan hệ: User 1-n AuditLog
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    log_id          BIGINT NOT NULL IDENTITY(1,1),
    user_id         INT    NULL,
    action          NVARCHAR(20) NOT NULL,
    entity_name     NVARCHAR(100)    NOT NULL,
    entity_id       NVARCHAR(50)     NULL,
    old_value       NVARCHAR(MAX)            NULL,
    new_value       NVARCHAR(MAX)            NULL,
    ip_address      NVARCHAR(45)     NULL,
    user_agent      NVARCHAR(255)    NULL,
    created_at      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (log_id),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : notifications - Thông báo
-- Mục đích: Thông báo gửi tới người dùng: lịch tiêm phòng, cảnh báo thể lực, khóa huấn luyện, sự cố...
-- Actor  : Tất cả
-- Quan hệ: User 1-n Notification
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id BIGINT NOT NULL IDENTITY(1,1),
    user_id         INT    NOT NULL,
    type            NVARCHAR(20) NOT NULL,
    title           NVARCHAR(200)    NOT NULL,
    content         NVARCHAR(MAX)            NULL,
    reference_type  NVARCHAR(50)     NULL,   -- polymorphic pointer (e.g. N'horse', N'injury')
    reference_id    BIGINT NULL,
    is_read         BIT         NOT NULL DEFAULT 0,
    created_at      DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);
GO

-- =====================================================================
-- 2. STABLE & HORSE PROFILE (Flow 1)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : stable_zones - Khu vực chuồng trại
-- Mục đích: Các khu chuồng của câu lạc bộ, mỗi khu do một nhân viên chuồng phụ trách.
-- Actor  : Groom, Club Manager
-- Quan hệ: User (Groom) 1-n StableZone ; StableZone 1-n Stall ; StableZone 1-n ZoneInventory
-- ---------------------------------------------------------------------
CREATE TABLE stable_zones (
    zone_id             INT NOT NULL IDENTITY(1,1),
    zone_code           NVARCHAR(20)  NOT NULL,
    zone_name           NVARCHAR(100) NOT NULL,
    description         NVARCHAR(255) NULL,
    responsible_user_id INT NULL,
    PRIMARY KEY (zone_id),
    CONSTRAINT uq_zone_code UNIQUE (zone_code),
    CONSTRAINT fk_zone_user FOREIGN KEY (responsible_user_id) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : stalls - Ô chuồng
-- Mục đích: Từng ô chuồng trong khu vực; dùng để vẽ sơ đồ phân bổ vị trí ngựa.
-- Actor  : Groom
-- Quan hệ: StableZone 1-n Stall ; Stall 1-1 Horse
-- ---------------------------------------------------------------------
CREATE TABLE stalls (
    stall_id    INT NOT NULL IDENTITY(1,1),
    zone_id     INT NOT NULL,
    stall_code  NVARCHAR(20)  NOT NULL,
    stall_type  NVARCHAR(20) NOT NULL DEFAULT N'STANDARD',
    status      NVARCHAR(20)         NOT NULL DEFAULT N'AVAILABLE',
    PRIMARY KEY (stall_id),
    CONSTRAINT uq_stall_code UNIQUE (stall_code),
    CONSTRAINT fk_stall_zone FOREIGN KEY (zone_id) REFERENCES stable_zones (zone_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : horses - Hồ sơ ngựa
-- Mục đích: Thông tin chiến mã: định danh, đặc điểm, cân nặng, trạng thái sức khỏe, sẵn sàng thi đấu, khóa huấn luyện.
-- Actor  : Tất cả
-- Quan hệ: User 1-n Horse (owner_id - sở hữu) ; User 1-n Horse (manager_id - quản lý) ; Stall 1-1 Horse
-- ---------------------------------------------------------------------
CREATE TABLE horses (
    horse_id            INT  NOT NULL IDENTITY(1,1),
    owner_id            INT  NOT NULL,   -- User sở hữu ngựa (Horse Owner)
    manager_id          INT  NULL,       -- User quản lý ngựa (Head Trainer phụ trách)
    stall_id            INT  NULL,
    name                NVARCHAR(100)  NOT NULL,
    registration_no     NVARCHAR(50)   NULL,
    microchip_no        NVARCHAR(50)   NULL,
    breed               NVARCHAR(100)  NULL,
    gender              NVARCHAR(20) NOT NULL,
    color               NVARCHAR(50)   NULL,
    date_of_birth       DATE          NULL,
    country_of_origin   NVARCHAR(100)  NULL,
    height_cm           DECIMAL(5,1)  NULL,
    current_weight_kg   DECIMAL(6,2)  NULL,
    health_status       NVARCHAR(20) NOT NULL DEFAULT N'ELIGIBLE',
    readiness_status    NVARCHAR(20)                  NOT NULL DEFAULT N'NOT_READY',
    is_training_locked  BIT       NOT NULL DEFAULT 0,
    photo_url           NVARCHAR(500)  NULL,
    status              NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    created_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (horse_id),
    CONSTRAINT uq_horse_stall UNIQUE (stall_id),              -- enforces 1-1 Stall <-> Horse
    CONSTRAINT uq_horse_registration UNIQUE (registration_no),
    CONSTRAINT uq_horse_microchip UNIQUE (microchip_no),
    CONSTRAINT fk_horse_owner   FOREIGN KEY (owner_id)   REFERENCES users (user_id),
    CONSTRAINT fk_horse_manager FOREIGN KEY (manager_id) REFERENCES users (user_id),
    CONSTRAINT fk_horse_stall   FOREIGN KEY (stall_id)   REFERENCES stalls (stall_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : horse_health_metrics - Chỉ số sức khỏe ngựa
-- Mục đích: Lịch sử đo cân nặng, nhịp tim nghỉ, thân nhiệt, nhịp thở theo thời gian.
-- Actor  : Veterinarian, Horse Owner
-- Quan hệ: Horse 1-n HealthMetric ; User 1-n HealthMetric (người ghi)
-- ---------------------------------------------------------------------
CREATE TABLE horse_health_metrics (
    metric_id           BIGINT NOT NULL IDENTITY(1,1),
    horse_id            INT    NOT NULL,
    recorded_by         INT    NULL,
    recorded_at         DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    weight_kg           DECIMAL(6,2)    NULL,
    resting_heart_rate  SMALLINT NULL,   -- bpm
    body_temperature    DECIMAL(4,1)    NULL,     -- °C
    respiratory_rate    SMALLINT NULL,   -- breaths/min
    notes               NVARCHAR(500)    NULL,
    PRIMARY KEY (metric_id),
    CONSTRAINT fk_metric_horse FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_metric_user  FOREIGN KEY (recorded_by) REFERENCES users (user_id)
);
GO

-- =====================================================================
-- 3. SUPPLY CATALOG & INVENTORY
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : supply_categories - Loại vật tư
-- Mục đích: Phân loại vật tư: Thức ăn (FEED), Thuốc (MEDICINE), Dụng cụ (EQUIPMENT).
-- Actor  : Club Manager
-- Quan hệ: SupplyCategory 1-n Supply
-- ---------------------------------------------------------------------
CREATE TABLE supply_categories (
    category_id     INT NOT NULL IDENTITY(1,1),
    category_name   NVARCHAR(100) NOT NULL,
    category_type   NVARCHAR(20) NOT NULL,
    description     NVARCHAR(255) NULL,
    PRIMARY KEY (category_id),
    CONSTRAINT uq_supply_category_name UNIQUE (category_name)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : supplies - Danh mục vật tư
-- Mục đích: Danh mục tổng vật tư y tế, thức ăn, dụng cụ kèm đơn vị, đơn giá, mức tồn tối thiểu.
-- Actor  : Club Manager
-- Quan hệ: SupplyCategory 1-n Supply ; Supply 1-n DietPlanItem / PrescriptionItem / ZoneInventory / SupplyRequestItem
-- ---------------------------------------------------------------------
CREATE TABLE supplies (
    supply_id       INT  NOT NULL IDENTITY(1,1),
    category_id     INT  NOT NULL,
    supply_code     NVARCHAR(30)   NOT NULL,
    supply_name     NVARCHAR(150)  NOT NULL,
    unit            NVARCHAR(20)   NOT NULL,       -- kg, bottle, box, piece...
    unit_price      DECIMAL(15,2) NOT NULL DEFAULT 0,
    min_stock_level DECIMAL(12,2) NOT NULL DEFAULT 0,
    description     NVARCHAR(255)  NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    PRIMARY KEY (supply_id),
    CONSTRAINT uq_supply_code UNIQUE (supply_code),
    CONSTRAINT fk_supply_category FOREIGN KEY (category_id) REFERENCES supply_categories (category_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : zone_inventories - Tồn kho vật tư theo khu vực
-- Mục đích: Số lượng vật tư hiện có tại từng khu chuồng (tách n-n StableZone - Supply).
-- Actor  : Groom
-- Quan hệ: StableZone 1-n ZoneInventory n-1 Supply
-- ---------------------------------------------------------------------
CREATE TABLE zone_inventories (
    inventory_id    INT  NOT NULL IDENTITY(1,1),
    zone_id         INT  NOT NULL,
    supply_id       INT  NOT NULL,
    quantity        DECIMAL(12,2) NOT NULL DEFAULT 0,
    updated_at      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (inventory_id),
    CONSTRAINT uq_zone_supply UNIQUE (zone_id, supply_id),
    CONSTRAINT fk_inv_zone   FOREIGN KEY (zone_id)   REFERENCES stable_zones (zone_id),
    CONSTRAINT fk_inv_supply FOREIGN KEY (supply_id) REFERENCES supplies (supply_id),
    CONSTRAINT chk_inv_qty CHECK (quantity >= 0)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : supply_requests - Phiếu đề xuất bổ sung vật tư
-- Mục đích: Nhân viên chuồng đề xuất bổ sung vật tư cho khu vực phụ trách; quản lý duyệt/từ chối.
-- Actor  : Groom, Club Manager
-- Quan hệ: StableZone 1-n SupplyRequest ; User 1-n SupplyRequest (người đề xuất / người duyệt)
-- ---------------------------------------------------------------------
CREATE TABLE supply_requests (
    request_id      INT NOT NULL IDENTITY(1,1),
    zone_id         INT NOT NULL,
    requested_by    INT NOT NULL,
    approved_by     INT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
    note            NVARCHAR(500) NULL,
    requested_at    DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    processed_at    DATETIME2     NULL,
    PRIMARY KEY (request_id),
    CONSTRAINT fk_sreq_zone      FOREIGN KEY (zone_id)      REFERENCES stable_zones (zone_id),
    CONSTRAINT fk_sreq_requester FOREIGN KEY (requested_by) REFERENCES users (user_id),
    CONSTRAINT fk_sreq_approver  FOREIGN KEY (approved_by)  REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : supply_request_items - Chi tiết phiếu đề xuất vật tư
-- Mục đích: Từng dòng vật tư và số lượng trong phiếu đề xuất (tách n-n SupplyRequest - Supply).
-- Actor  : Groom
-- Quan hệ: SupplyRequest 1-n SupplyRequestItem n-1 Supply
-- ---------------------------------------------------------------------
CREATE TABLE supply_request_items (
    request_item_id INT  NOT NULL IDENTITY(1,1),
    request_id      INT  NOT NULL,
    supply_id       INT  NOT NULL,
    quantity        DECIMAL(12,2) NOT NULL,
    note            NVARCHAR(255)  NULL,
    PRIMARY KEY (request_item_id),
    CONSTRAINT uq_sreq_item UNIQUE (request_id, supply_id),
    CONSTRAINT fk_sreqi_request FOREIGN KEY (request_id) REFERENCES supply_requests (request_id),
    CONSTRAINT fk_sreqi_supply  FOREIGN KEY (supply_id)  REFERENCES supplies (supply_id),
    CONSTRAINT chk_sreqi_qty CHECK (quantity > 0)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : inventory_transactions - Lịch sử biến động kho vật tư
-- Mục đích: Ghi nhận lịch sử nhập kho, xuất kho, điều chỉnh kiểm kê, tiêu hao (cho ăn/y tế) tại từng khu vực chuồng.
-- Actor  : Groom, Club Manager
-- Quan hệ: StableZone 1-n InventoryTransaction ; Supply 1-n InventoryTransaction ; User 1-n InventoryTransaction
-- ---------------------------------------------------------------------
CREATE TABLE inventory_transactions (
    transaction_id      BIGINT NOT NULL IDENTITY(1,1),
    zone_id             INT    NOT NULL,
    supply_id           INT    NOT NULL,
    performed_by        INT    NULL,
    transaction_type    NVARCHAR(20) NOT NULL,
    quantity            DECIMAL(12,2)   NOT NULL,
    balance_after       DECIMAL(12,2)   NULL,
    reference_type      NVARCHAR(50)     NULL,   -- N'SUPPLY_REQUEST', N'CARE_TASK', N'TREATMENT'
    reference_id        BIGINT NULL,
    notes               NVARCHAR(500)    NULL,
    transaction_date    DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_inv_tx_zone   FOREIGN KEY (zone_id)      REFERENCES stable_zones (zone_id),
    CONSTRAINT fk_inv_tx_supply FOREIGN KEY (supply_id)    REFERENCES supplies (supply_id),
    CONSTRAINT fk_inv_tx_user   FOREIGN KEY (performed_by) REFERENCES users (user_id),
    CONSTRAINT chk_inv_tx_qty CHECK (quantity > 0)
);
GO

-- =====================================================================
-- 4. TRAINING (Flow 2)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : training_plans - Giáo án huấn luyện
-- Mục đích: Giáo án tổng do HLV Trưởng lập cho từng con ngựa, có mục tiêu và khoảng thời gian.
-- Actor  : Head Trainer
-- Quan hệ: Horse 1-n TrainingPlan ; User (Head Trainer) 1-n TrainingPlan ; TrainingPlan 1-n TrainingPhase
-- ---------------------------------------------------------------------
CREATE TABLE training_plans (
    plan_id     INT NOT NULL IDENTITY(1,1),
    horse_id    INT NOT NULL,
    trainer_id  INT NOT NULL,
    plan_name   NVARCHAR(150) NOT NULL,
    goal        NVARCHAR(500) NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE         NULL,
    status      NVARCHAR(20) NOT NULL DEFAULT N'DRAFT',
    created_at  DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    updated_at  DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (plan_id),
    CONSTRAINT fk_plan_horse   FOREIGN KEY (horse_id)   REFERENCES horses (horse_id),
    CONSTRAINT fk_plan_trainer FOREIGN KEY (trainer_id) REFERENCES users (user_id),
    CONSTRAINT chk_plan_dates CHECK (end_date IS NULL OR end_date >= start_date)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : training_phases - Giai đoạn huấn luyện
-- Mục đích: Các giai đoạn của giáo án (Base/Build/Peak/Taper/Recovery) với cự ly, khối lượng, mặt sân mục tiêu.
-- Actor  : Head Trainer
-- Quan hệ: TrainingPlan 1-n TrainingPhase ; TrainingPhase 1-n TrainingSession
-- ---------------------------------------------------------------------
CREATE TABLE training_phases (
    phase_id            INT  NOT NULL IDENTITY(1,1),
    plan_id             INT  NOT NULL,
    phase_order         TINYINT NOT NULL,
    phase_name          NVARCHAR(100)  NOT NULL,
    phase_type          NVARCHAR(20) NOT NULL,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    target_distance_m   INT  NULL,        -- cự ly mục tiêu
    target_volume       NVARCHAR(100)  NULL,        -- khối lượng (VD: 5 buổi/tuần, 20km/tuần)
    track_surface       NVARCHAR(20) NULL,  -- mặt sân
    notes               NVARCHAR(500)  NULL,
    PRIMARY KEY (phase_id),
    CONSTRAINT uq_phase_order UNIQUE (plan_id, phase_order),
    CONSTRAINT fk_phase_plan FOREIGN KEY (plan_id) REFERENCES training_plans (plan_id),
    CONSTRAINT chk_phase_dates CHECK (end_date >= start_date)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : training_sessions - Buổi tập / lượt chạy thử
-- Mục đích: Lịch tập hằng ngày được phân công cho nhân sự; gồm cả lượt chạy thử và video buổi tập.
-- Actor  : Head Trainer, Groom, Horse Owner
-- Quan hệ: Horse 1-n TrainingSession ; TrainingPhase 1-n TrainingSession ; User (staff) 1-n TrainingSession
-- ---------------------------------------------------------------------
CREATE TABLE training_sessions (
    session_id          INT  NOT NULL IDENTITY(1,1),
    horse_id            INT  NOT NULL,   -- Chiến mã tham gia buổi tập / chạy thử
    phase_id            INT  NULL,       -- Thuộc giai đoạn giáo án (NULL nếu chạy thử/tập đột xuất ngoài giáo án)
    assigned_staff_id   INT  NULL,
    session_date        DATE          NOT NULL,
    start_time          TIME          NULL,
    end_time            TIME          NULL,
    session_type        NVARCHAR(20) NOT NULL DEFAULT N'WORKOUT',
    intensity           NVARCHAR(20)              NOT NULL DEFAULT N'MODERATE',
    planned_distance_m  INT  NULL,
    track_surface       NVARCHAR(20) NULL,
    instructions        NVARCHAR(500)  NULL,
    video_url           NVARCHAR(500)  NULL,        -- video buổi đua thử
    status              NVARCHAR(20) NOT NULL DEFAULT N'SCHEDULED',
    created_at          DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (session_id),
    CONSTRAINT fk_session_horse FOREIGN KEY (horse_id)          REFERENCES horses (horse_id),
    CONSTRAINT fk_session_phase FOREIGN KEY (phase_id)          REFERENCES training_phases (phase_id),
    CONSTRAINT fk_session_staff FOREIGN KEY (assigned_staff_id) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : session_evaluations - Đánh giá buổi tập
-- Mục đích: Chỉ số thực tế (cự ly, tốc độ, nhịp tim) và nhận xét chuyên môn của HLV sau mỗi buổi tập.
-- Actor  : Head Trainer, Horse Owner
-- Quan hệ: TrainingSession 1-1 SessionEvaluation ; User (Head Trainer) 1-n SessionEvaluation
-- ---------------------------------------------------------------------
CREATE TABLE session_evaluations (
    evaluation_id       INT  NOT NULL IDENTITY(1,1),
    session_id          INT  NOT NULL,
    evaluator_id        INT  NOT NULL,
    actual_distance_m   INT  NULL,
    duration_sec        INT  NULL,
    avg_speed_kmh       DECIMAL(5,2)  NULL,
    max_speed_kmh       DECIMAL(5,2)  NULL,
    avg_heart_rate      SMALLINT NULL,
    max_heart_rate      SMALLINT NULL,
    recovery_heart_rate SMALLINT NULL,
    performance_rating  TINYINT  NULL,    -- 1..10
    comment             NVARCHAR(MAX)          NULL,
    evaluated_at        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (evaluation_id),
    CONSTRAINT uq_eval_session UNIQUE (session_id),       -- enforces 1-1
    CONSTRAINT fk_eval_session   FOREIGN KEY (session_id)   REFERENCES training_sessions (session_id),
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES users (user_id),
    CONSTRAINT chk_eval_rating CHECK (performance_rating IS NULL OR performance_rating BETWEEN 1 AND 10)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : sensor_readings - Dữ liệu cảm biến realtime
-- Mục đích: Nhịp tim, vận tốc, quãng đường, tọa độ GPS ghi nhận liên tục trong buổi tập.
-- Actor  : Hệ thống, Head Trainer
-- Quan hệ: TrainingSession 1-n SensorReading
-- ---------------------------------------------------------------------
CREATE TABLE sensor_readings (
    reading_id      BIGINT NOT NULL IDENTITY(1,1),
    session_id      INT    NOT NULL,
    recorded_at     DATETIME2(3)     NOT NULL,
    heart_rate      SMALLINT NULL,
    speed_kmh       DECIMAL(5,2)    NULL,
    distance_m      DECIMAL(8,2)    NULL,
    latitude        DECIMAL(10,7)   NULL,
    longitude       DECIMAL(10,7)   NULL,
    PRIMARY KEY (reading_id),
    CONSTRAINT fk_reading_session FOREIGN KEY (session_id) REFERENCES training_sessions (session_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : performance_alerts - Cảnh báo thể lực
-- Mục đích: Cảnh báo vượt ngưỡng nhịp tim/vận tốc hoặc nguy cơ chấn thương dựa trên dữ liệu realtime.
-- Actor  : Head Trainer
-- Quan hệ: Horse 1-n PerformanceAlert ; TrainingSession 1-n PerformanceAlert ; User 1-n PerformanceAlert (xác nhận)
-- ---------------------------------------------------------------------
CREATE TABLE performance_alerts (
    alert_id         BIGINT NOT NULL IDENTITY(1,1),
    horse_id         INT    NOT NULL,
    session_id       INT    NULL,
    alert_type       NVARCHAR(20) NOT NULL,
    severity         NVARCHAR(20) NOT NULL DEFAULT N'MEDIUM',
    threshold_value  DECIMAL(8,2)    NULL,
    actual_value     DECIMAL(8,2)    NULL,
    message          NVARCHAR(500)    NOT NULL,
    is_acknowledged  BIT         NOT NULL DEFAULT 0,
    acknowledged_by  INT    NULL,
    acknowledged_at  DATETIME2        NULL,
    created_at       DATETIME2        NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (alert_id),
    CONSTRAINT fk_alert_horse   FOREIGN KEY (horse_id)        REFERENCES horses (horse_id),
    CONSTRAINT fk_alert_session FOREIGN KEY (session_id)      REFERENCES training_sessions (session_id),
    CONSTRAINT fk_alert_ack     FOREIGN KEY (acknowledged_by) REFERENCES users (user_id)
);
GO

-- =====================================================================
-- 5. DAILY CARE & INCIDENTS (Flow 4)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : incident_reports - Báo cáo sự cố tại chuồng
-- Mục đích: Sự cố đột xuất: ngựa bỏ ăn, đau bụng, sốt, móng bị xước... do nhân viên chuồng gửi.
-- Actor  : Groom, Veterinarian
-- Quan hệ: Horse 1-n IncidentReport ; User (Groom) 1-n IncidentReport ; IncidentReport 1-1 MedicalRecord
-- ---------------------------------------------------------------------
CREATE TABLE incident_reports (
    incident_id     INT NOT NULL IDENTITY(1,1),
    horse_id        INT NOT NULL,
    reported_by     INT NOT NULL,
    incident_type   NVARCHAR(20) NOT NULL,
    severity        NVARCHAR(20) NOT NULL DEFAULT N'MEDIUM',
    description     NVARCHAR(MAX)         NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'OPEN',
    reported_at     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    resolved_at     DATETIME2     NULL,
    PRIMARY KEY (incident_id),
    CONSTRAINT fk_incident_horse    FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_incident_reporter FOREIGN KEY (reported_by) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : incident_images - Hình ảnh sự cố
-- Mục đích: Ảnh thực tế đính kèm báo cáo sự cố.
-- Actor  : Groom
-- Quan hệ: IncidentReport 1-n IncidentImage
-- ---------------------------------------------------------------------
CREATE TABLE incident_images (
    image_id        INT NOT NULL IDENTITY(1,1),
    incident_id     INT NOT NULL,
    image_url       NVARCHAR(500) NOT NULL,
    uploaded_at     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (image_id),
    CONSTRAINT fk_incimg_incident FOREIGN KEY (incident_id) REFERENCES incident_reports (incident_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : diet_plans - Khẩu phần ăn
-- Mục đích: Khẩu phần ăn của ngựa trong một khoảng thời gian, cần được duyệt trước khi áp dụng.
-- Actor  : Groom, Veterinarian
-- Quan hệ: Horse 1-n DietPlan ; User 1-n DietPlan (người lập / người duyệt) ; DietPlan 1-n DietPlanItem
-- ---------------------------------------------------------------------
CREATE TABLE diet_plans (
    diet_plan_id    INT NOT NULL IDENTITY(1,1),
    horse_id        INT NOT NULL,
    created_by      INT NOT NULL,
    approved_by     INT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE         NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'DRAFT',
    notes           NVARCHAR(500) NULL,
    created_at      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    approved_at     DATETIME2     NULL,
    PRIMARY KEY (diet_plan_id),
    CONSTRAINT fk_diet_horse    FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_diet_creator  FOREIGN KEY (created_by)  REFERENCES users (user_id),
    CONSTRAINT fk_diet_approver FOREIGN KEY (approved_by) REFERENCES users (user_id),
    CONSTRAINT chk_diet_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : diet_plan_items - Chi tiết khẩu phần theo bữa
-- Mục đích: Từng loại thức ăn (ngũ cốc, cỏ, vitamin) và định lượng cho từng bữa (tách n-n DietPlan - Supply).
-- Actor  : Groom
-- Quan hệ: DietPlan 1-n DietPlanItem n-1 Supply
-- ---------------------------------------------------------------------
CREATE TABLE diet_plan_items (
    diet_item_id    INT  NOT NULL IDENTITY(1,1),
    diet_plan_id    INT  NOT NULL,
    supply_id       INT  NOT NULL,
    meal_type       NVARCHAR(20) NOT NULL,
    meal_time       TIME          NULL,
    quantity        DECIMAL(10,2) NOT NULL,
    unit            NVARCHAR(20)   NOT NULL,
    note            NVARCHAR(255)  NULL,
    PRIMARY KEY (diet_item_id),
    CONSTRAINT fk_dieti_plan   FOREIGN KEY (diet_plan_id) REFERENCES diet_plans (diet_plan_id),
    CONSTRAINT fk_dieti_supply FOREIGN KEY (supply_id)    REFERENCES supplies (supply_id),
    CONSTRAINT chk_dieti_qty CHECK (quantity > 0)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : care_tasks - Công việc chăm sóc hằng ngày
-- Mục đích: Việc cần làm cho từng con ngựa (cho ăn, vệ sinh chuồng, tắm, ngâm chân nước đá) và xác nhận hoàn thành.
-- Actor  : Groom
-- Quan hệ: Horse 1-n CareTask ; User (Groom) 1-n CareTask (người được giao / người xác nhận hoàn thành)
-- ---------------------------------------------------------------------
CREATE TABLE care_tasks (
    task_id         INT NOT NULL IDENTITY(1,1),
    horse_id        INT NOT NULL,
    assigned_to     INT NULL,          -- Nhân viên chăm sóc được phân công
    completed_by    INT NULL,          -- Người thực hiện / xác nhận hoàn thành
    task_type       NVARCHAR(20) NOT NULL,
    scheduled_at    DATETIME2     NOT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
    completed_at    DATETIME2     NULL,
    note            NVARCHAR(255) NULL,
    PRIMARY KEY (task_id),
    CONSTRAINT fk_task_horse     FOREIGN KEY (horse_id)     REFERENCES horses (horse_id),
    CONSTRAINT fk_task_user      FOREIGN KEY (assigned_to)  REFERENCES users (user_id),
    CONSTRAINT fk_task_completer FOREIGN KEY (completed_by) REFERENCES users (user_id)
);
GO

-- =====================================================================
-- 6. MEDICAL & INJURY (Flow 3)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : medical_records - Hồ sơ khám bệnh
-- Mục đích: Lần khám của bác sĩ thú y: lý do, triệu chứng, chẩn đoán, trạng thái sức khỏe sau khám.
-- Actor  : Veterinarian, Horse Owner
-- Quan hệ: Horse 1-n MedicalRecord ; User (Vet) 1-n MedicalRecord ; IncidentReport 1-1 MedicalRecord
-- ---------------------------------------------------------------------
CREATE TABLE medical_records (
    record_id           INT NOT NULL IDENTITY(1,1),
    horse_id            INT NOT NULL,
    vet_id              INT NOT NULL,
    incident_id         INT NULL,
    exam_date           DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    reason              NVARCHAR(255) NULL,
    symptoms            NVARCHAR(MAX)         NULL,
    diagnosis           NVARCHAR(MAX)         NULL,
    health_status_after NVARCHAR(20) NOT NULL,
    notes               NVARCHAR(MAX)         NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT uq_medical_incident UNIQUE (incident_id),  -- enforces 1-1 with incident
    CONSTRAINT fk_medical_horse    FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_medical_vet      FOREIGN KEY (vet_id)      REFERENCES users (user_id),
    CONSTRAINT fk_medical_incident FOREIGN KEY (incident_id) REFERENCES incident_reports (incident_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : treatment_plans - Phác đồ điều trị
-- Mục đích: Phác đồ điều trị phát sinh từ một lần khám; có thể cập nhật nhiều phác đồ theo diễn biến.
-- Actor  : Veterinarian
-- Quan hệ: MedicalRecord 1-n TreatmentPlan ; TreatmentPlan 1-n PrescriptionItem
-- ---------------------------------------------------------------------
CREATE TABLE treatment_plans (
    treatment_id    INT NOT NULL IDENTITY(1,1),
    record_id       INT NOT NULL,
    description     NVARCHAR(MAX)         NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    created_at      DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (treatment_id),
    CONSTRAINT fk_treat_record FOREIGN KEY (record_id) REFERENCES medical_records (record_id),
    CONSTRAINT chk_treat_dates CHECK (end_date IS NULL OR end_date >= start_date)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : prescription_items - Đơn thuốc
-- Mục đích: Từng loại thuốc trong phác đồ: liều lượng, tần suất, số ngày, đường dùng (tách n-n TreatmentPlan - Supply).
-- Actor  : Veterinarian
-- Quan hệ: TreatmentPlan 1-n PrescriptionItem n-1 Supply
-- ---------------------------------------------------------------------
CREATE TABLE prescription_items (
    prescription_item_id INT NOT NULL IDENTITY(1,1),
    treatment_id         INT NOT NULL,
    supply_id            INT NOT NULL,
    dosage               NVARCHAR(100) NOT NULL,      -- VD: 10ml
    frequency            NVARCHAR(100) NOT NULL,      -- VD: 2 lần/ngày
    duration_days        SMALLINT NULL,
    route                NVARCHAR(20) NULL,
    instructions         NVARCHAR(255) NULL,
    PRIMARY KEY (prescription_item_id),
    CONSTRAINT fk_presc_treatment FOREIGN KEY (treatment_id) REFERENCES treatment_plans (treatment_id),
    CONSTRAINT fk_presc_supply    FOREIGN KEY (supply_id)    REFERENCES supplies (supply_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : injuries - Chấn thương
-- Mục đích: Chấn thương được đánh dấu trên mô hình cơ/xương 3D (mesh + tọa độ x,y,z), mức độ và trạng thái phục hồi.
-- Actor  : Veterinarian
-- Quan hệ: MedicalRecord 1-n Injury ; Injury 1-n InjuryProgressLog ; Injury 1-n TrainingLock
-- ---------------------------------------------------------------------
CREATE TABLE injuries (
    injury_id       INT NOT NULL IDENTITY(1,1),
    record_id       INT NOT NULL,
    body_part       NVARCHAR(100) NOT NULL,           -- VD: Left foreleg - cannon bone
    body_system     NVARCHAR(20) NOT NULL,
    model_mesh_id   NVARCHAR(100) NULL,               -- id of mesh on 3D model
    position_x      DECIMAL(9,4) NULL,
    position_y      DECIMAL(9,4) NULL,
    position_z      DECIMAL(9,4) NULL,
    injury_type     NVARCHAR(100) NULL,               -- fracture, strain, laceration...
    severity        NVARCHAR(20) NOT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    occurred_date   DATE         NULL,
    healed_date     DATE         NULL,
    description     NVARCHAR(MAX)         NULL,
    PRIMARY KEY (injury_id),
    CONSTRAINT fk_injury_record FOREIGN KEY (record_id) REFERENCES medical_records (record_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : injury_progress_logs - Diễn biến phục hồi chấn thương
-- Mục đích: Nhật ký theo dõi phục hồi: % hồi phục, mức đau, ghi chú, hình ảnh.
-- Actor  : Veterinarian
-- Quan hệ: Injury 1-n InjuryProgressLog ; User 1-n InjuryProgressLog
-- ---------------------------------------------------------------------
CREATE TABLE injury_progress_logs (
    progress_id      INT NOT NULL IDENTITY(1,1),
    injury_id        INT NOT NULL,
    logged_by        INT NULL,
    log_date         DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    recovery_percent TINYINT NULL,          -- 0..100
    pain_level       TINYINT NULL,          -- 0..10
    notes            NVARCHAR(MAX)         NULL,
    image_url        NVARCHAR(500) NULL,
    PRIMARY KEY (progress_id),
    CONSTRAINT fk_progress_injury FOREIGN KEY (injury_id) REFERENCES injuries (injury_id),
    CONSTRAINT fk_progress_user   FOREIGN KEY (logged_by) REFERENCES users (user_id),
    CONSTRAINT chk_progress_pct  CHECK (recovery_percent IS NULL OR recovery_percent <= 100),
    CONSTRAINT chk_progress_pain CHECK (pain_level IS NULL OR pain_level <= 10)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : training_locks - Lệnh khóa huấn luyện
-- Mục đích: Lệnh khẩn cấp của bác sĩ thú y chặn xếp lịch bài tập nặng (hoặc toàn bộ) cho ngựa chấn thương.
-- Actor  : Veterinarian, Head Trainer
-- Quan hệ: Horse 1-n TrainingLock ; User (Vet) 1-n TrainingLock ; Injury 1-n TrainingLock
-- ---------------------------------------------------------------------
CREATE TABLE training_locks (
    lock_id         INT NOT NULL IDENTITY(1,1),
    horse_id        INT NOT NULL,
    locked_by       INT NOT NULL,
    injury_id       INT NULL,
    lock_level      NVARCHAR(20) NOT NULL DEFAULT N'HEAVY_ONLY',  -- HEAVY_ONLY: chặn bài tập nặng
    reason          NVARCHAR(500) NOT NULL,
    locked_at       DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    expected_end_at DATETIME2     NULL,
    released_at     DATETIME2     NULL,
    released_by     INT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'ACTIVE',
    PRIMARY KEY (lock_id),
    CONSTRAINT fk_lock_horse    FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_lock_vet      FOREIGN KEY (locked_by)   REFERENCES users (user_id),
    CONSTRAINT fk_lock_injury   FOREIGN KEY (injury_id)   REFERENCES injuries (injury_id),
    CONSTRAINT fk_lock_releaser FOREIGN KEY (released_by) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : preventive_care_schedules - Lịch chăm sóc định kỳ
-- Mục đích: Lịch tiêm phòng, tẩy giun, kiểm tra móng (farrier), nha khoa; dùng để gửi thông báo tự động.
-- Actor  : Veterinarian
-- Quan hệ: Horse 1-n PreventiveCare ; User 1-n PreventiveCare (người thực hiện)
-- ---------------------------------------------------------------------
CREATE TABLE preventive_care_schedules (
    schedule_id     INT NOT NULL IDENTITY(1,1),
    horse_id        INT NOT NULL,
    care_type       NVARCHAR(20) NOT NULL,
    description     NVARCHAR(255) NULL,
    due_date        DATE         NOT NULL,
    interval_days   SMALLINT NULL,          -- chu kỳ lặp lại
    remind_before_days TINYINT NOT NULL DEFAULT 3,
    status          NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
    completed_date  DATE         NULL,
    performed_by    INT NULL,
    notes           NVARCHAR(500) NULL,
    PRIMARY KEY (schedule_id),
    CONSTRAINT fk_care_horse FOREIGN KEY (horse_id)     REFERENCES horses (horse_id),
    CONSTRAINT fk_care_user  FOREIGN KEY (performed_by) REFERENCES users (user_id)
);
GO

-- =====================================================================
-- 7. RACING & FINANCE (Flow 5)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Bảng   : tournaments - Giải đua
-- Mục đích: Thông tin giải đấu: đơn vị tổ chức, địa điểm, thời gian.
-- Actor  : Head Trainer, Club Manager
-- Quan hệ: Tournament 1-n Race
-- ---------------------------------------------------------------------
CREATE TABLE tournaments (
    tournament_id   INT NOT NULL IDENTITY(1,1),
    name            NVARCHAR(150) NOT NULL,
    organizer       NVARCHAR(150) NULL,
    location        NVARCHAR(255) NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'UPCOMING',
    description     NVARCHAR(MAX)         NULL,
    PRIMARY KEY (tournament_id),
    CONSTRAINT chk_tour_dates CHECK (end_date >= start_date)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : races - Cuộc đua
-- Mục đích: Từng cuộc đua trong giải: cự ly, mặt sân, hạng, tổng giải thưởng, phí và hạn đăng ký.
-- Actor  : Head Trainer
-- Quan hệ: Tournament 1-n Race ; Race 1-n RaceRegistration
-- ---------------------------------------------------------------------
CREATE TABLE races (
    race_id                 INT  NOT NULL IDENTITY(1,1),
    tournament_id           INT  NOT NULL,
    race_name               NVARCHAR(150)  NOT NULL,
    race_datetime           DATETIME2      NOT NULL,
    distance_m              INT  NOT NULL,
    track_surface           NVARCHAR(20) NOT NULL,
    race_class              NVARCHAR(50)   NULL,     -- Group 1, Handicap...
    min_age                 TINYINT NULL,
    max_age                 TINYINT NULL,
    prize_pool              DECIMAL(15,2) NOT NULL DEFAULT 0,
    entry_fee               DECIMAL(15,2) NOT NULL DEFAULT 0,
    max_runners             TINYINT NULL,
    registration_deadline   DATETIME2      NULL,
    status                  NVARCHAR(20) NOT NULL DEFAULT N'OPEN',
    PRIMARY KEY (race_id),
    CONSTRAINT fk_race_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : race_registrations - Đăng ký thi đấu
-- Mục đích: Đăng ký ngựa tham gia cuộc đua do HLV Trưởng thực hiện (tách n-n Race - Horse).
-- Actor  : Head Trainer
-- Quan hệ: Race 1-n RaceRegistration n-1 Horse ; User (Head Trainer) 1-n RaceRegistration ; RaceRegistration 1-1 RaceResult
-- ---------------------------------------------------------------------
CREATE TABLE race_registrations (
    registration_id INT NOT NULL IDENTITY(1,1),
    race_id         INT NOT NULL,
    horse_id        INT NOT NULL,
    registered_by   INT NOT NULL,
    jockey_name     NVARCHAR(100) NULL,
    gate_number     TINYINT NULL,
    status          NVARCHAR(20) NOT NULL DEFAULT N'PENDING',
    registered_at   DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    note            NVARCHAR(255) NULL,
    PRIMARY KEY (registration_id),
    CONSTRAINT uq_race_horse UNIQUE (race_id, horse_id),
    CONSTRAINT fk_reg_race    FOREIGN KEY (race_id)       REFERENCES races (race_id),
    CONSTRAINT fk_reg_horse   FOREIGN KEY (horse_id)      REFERENCES horses (horse_id),
    CONSTRAINT fk_reg_trainer FOREIGN KEY (registered_by) REFERENCES users (user_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : race_results - Kết quả thi đấu
-- Mục đích: Thứ hạng, thời gian về đích, tiền thưởng và video của một lượt đăng ký thi đấu.
-- Actor  : Horse Owner, Club Manager
-- Quan hệ: RaceRegistration 1-1 RaceResult
-- ---------------------------------------------------------------------
CREATE TABLE race_results (
    result_id        INT  NOT NULL IDENTITY(1,1),
    registration_id  INT  NOT NULL,
    finish_position  TINYINT NULL,   -- NULL if DNF
    finish_time_sec  DECIMAL(8,3)  NULL,
    is_dnf           BIT       NOT NULL DEFAULT 0,
    prize_money      DECIMAL(15,2) NOT NULL DEFAULT 0,
    video_url        NVARCHAR(500)  NULL,
    notes            NVARCHAR(500)  NULL,
    recorded_at      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (result_id),
    CONSTRAINT uq_result_registration UNIQUE (registration_id),   -- enforces 1-1
    CONSTRAINT fk_result_reg FOREIGN KEY (registration_id) REFERENCES race_registrations (registration_id)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : expenses - Chi phí
-- Mục đích: Chi phí phát sinh cho từng con ngựa: thức ăn, y tế, huấn luyện, chuồng trại, phí đua...
-- Actor  : Horse Owner, Club Manager
-- Quan hệ: Horse 1-n Expense ; User 1-n Expense (người ghi)
-- ---------------------------------------------------------------------
CREATE TABLE expenses (
    expense_id      INT  NOT NULL IDENTITY(1,1),
    horse_id        INT  NOT NULL,
    recorded_by     INT  NULL,
    category        NVARCHAR(20) NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    expense_date    DATE          NOT NULL,
    description     NVARCHAR(255)  NULL,
    created_at      DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (expense_id),
    CONSTRAINT fk_expense_horse FOREIGN KEY (horse_id)    REFERENCES horses (horse_id),
    CONSTRAINT fk_expense_user  FOREIGN KEY (recorded_by) REFERENCES users (user_id),
    CONSTRAINT chk_expense_amount CHECK (amount >= 0)
);
GO

-- ---------------------------------------------------------------------
-- Bảng   : owner_reports - Báo cáo định kỳ cho chủ ngựa
-- Mục đích: Báo cáo tổng hợp chi phí nuôi dưỡng, y tế và doanh thu tiền thưởng theo kỳ.
-- Actor  : Horse Owner
-- Quan hệ: User (Owner) 1-n OwnerReport ; Horse 1-n OwnerReport
-- ---------------------------------------------------------------------
CREATE TABLE owner_reports (
    report_id           INT  NOT NULL IDENTITY(1,1),
    owner_id            INT  NOT NULL,
    horse_id            INT  NULL,          -- NULL = report for all horses of owner
    period_start        DATE          NOT NULL,
    period_end          DATE          NOT NULL,
    total_feed_cost     DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_medical_cost  DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_other_cost    DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_prize_revenue DECIMAL(15,2) NOT NULL DEFAULT 0,
    file_url            NVARCHAR(500)  NULL,
    generated_at        DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    PRIMARY KEY (report_id),
    CONSTRAINT fk_report_owner FOREIGN KEY (owner_id) REFERENCES users (user_id),
    CONSTRAINT fk_report_horse FOREIGN KEY (horse_id) REFERENCES horses (horse_id),
    CONSTRAINT chk_report_period CHECK (period_end >= period_start)
);
GO

-- =====================================================================
-- 8. SEED DATA (master data)
-- =====================================================================

INSERT INTO roles (role_code, role_name, description) VALUES
    (N'HEAD_TRAINER', N'Head Trainer',        N'Huấn luyện viên Trưởng'),
    (N'VETERINARIAN', N'Veterinarian',        N'Bác sĩ Thú y'),
    (N'GROOM',        N'Groom / Stable Hand', N'Nhân viên Chăm sóc & Chuồng trại'),
    (N'HORSE_OWNER',  N'Horse Owner',         N'Chủ sở hữu Ngựa'),
    (N'CLUB_MANAGER', N'Club Manager',        N'Quản lý Câu lạc bộ');

INSERT INTO permissions (permission_code, module, description) VALUES
    (N'HORSE_VIEW',            N'HORSE',     N'Xem hồ sơ ngựa'),
    (N'HORSE_MANAGE',          N'HORSE',     N'Thêm/sửa/xóa hồ sơ ngựa'),
    (N'TRAINING_VIEW',         N'TRAINING',  N'Xem giáo án & lịch tập'),
    (N'TRAINING_MANAGE',       N'TRAINING',  N'Lập giáo án, phân công lịch tập'),
    (N'TRAINING_EVALUATE',     N'TRAINING',  N'Đánh giá buổi tập'),
    (N'MEDICAL_VIEW',          N'MEDICAL',   N'Xem hồ sơ y tế'),
    (N'MEDICAL_MANAGE',        N'MEDICAL',   N'Ghi nhận khám bệnh, phác đồ, chấn thương'),
    (N'TRAINING_LOCK',         N'MEDICAL',   N'Đặt / gỡ lệnh khóa huấn luyện'),
    (N'CARE_VIEW',             N'CARE',      N'Xem lịch chăm sóc & khẩu phần'),
    (N'CARE_EXECUTE',          N'CARE',      N'Xác nhận hoàn thành công việc chăm sóc'),
    (N'DIET_APPROVE',          N'CARE',      N'Duyệt khẩu phần ăn'),
    (N'INCIDENT_REPORT',       N'CARE',      N'Gửi báo cáo sự cố'),
    (N'SUPPLY_VIEW',           N'SUPPLY',    N'Xem vật tư'),
    (N'SUPPLY_REQUEST',        N'SUPPLY',    N'Đề xuất bổ sung vật tư'),
    (N'SUPPLY_MANAGE',         N'SUPPLY',    N'Quản lý danh mục & duyệt vật tư'),
    (N'RACE_VIEW',             N'RACE',      N'Xem giải đấu & thành tích'),
    (N'RACE_REGISTER',         N'RACE',      N'Đăng ký ngựa tham gia giải'),
    (N'FINANCE_VIEW',          N'FINANCE',   N'Xem chi phí & doanh thu'),
    (N'REPORT_VIEW',           N'REPORT',    N'Xem báo cáo tổng quan'),
    (N'USER_MANAGE',           N'SYSTEM',    N'Quản lý nhân sự & tài khoản'),
    (N'RBAC_MANAGE',           N'SYSTEM',    N'Phân quyền vai trò'),
    (N'AUDIT_VIEW',            N'SYSTEM',    N'Xem nhật ký thao tác');

-- Role -> Permission mapping
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
JOIN permissions p ON
    (r.role_code = N'HEAD_TRAINER' AND p.permission_code IN
        (N'HORSE_VIEW',N'TRAINING_VIEW',N'TRAINING_MANAGE',N'TRAINING_EVALUATE',N'MEDICAL_VIEW',
         N'CARE_VIEW',N'RACE_VIEW',N'RACE_REGISTER',N'REPORT_VIEW'))
 OR (r.role_code = N'VETERINARIAN' AND p.permission_code IN
        (N'HORSE_VIEW',N'TRAINING_VIEW',N'MEDICAL_VIEW',N'MEDICAL_MANAGE',N'TRAINING_LOCK',
         N'CARE_VIEW',N'DIET_APPROVE',N'SUPPLY_VIEW'))
 OR (r.role_code = N'GROOM' AND p.permission_code IN
        (N'HORSE_VIEW',N'TRAINING_VIEW',N'CARE_VIEW',N'CARE_EXECUTE',N'INCIDENT_REPORT',
         N'SUPPLY_VIEW',N'SUPPLY_REQUEST'))
 OR (r.role_code = N'HORSE_OWNER' AND p.permission_code IN
        (N'HORSE_VIEW',N'TRAINING_VIEW',N'MEDICAL_VIEW',N'RACE_VIEW',N'FINANCE_VIEW'))
 OR (r.role_code = N'CLUB_MANAGER');   -- full access

INSERT INTO supply_categories (category_name, category_type, description) VALUES
    (N'Ngũ cốc',        N'FEED',      N'Yến mạch, bắp, lúa mạch...'),
    (N'Cỏ khô',         N'FEED',      N'Cỏ Timothy, cỏ Alfalfa...'),
    (N'Vitamin & Khoáng',N'FEED',     N'Thực phẩm bổ sung'),
    (N'Thuốc',          N'MEDICINE',  N'Kháng sinh, kháng viêm, giảm đau...'),
    (N'Vắc-xin',        N'MEDICINE',  N'Vắc-xin định kỳ'),
    (N'Dụng cụ',        N'EQUIPMENT', N'Yên, cương, móng sắt, dụng cụ vệ sinh...');

-- ============================================================
-- Converted non-unique indexes
-- ============================================================
CREATE INDEX idx_audit_entity ON audit_logs (entity_name, entity_id);
GO
CREATE INDEX idx_audit_created ON audit_logs (created_at);
GO
CREATE INDEX idx_notif_user_read ON notifications (user_id, is_read);
GO
CREATE INDEX idx_horse_health ON horses (health_status);
GO
CREATE INDEX idx_metric_horse_time ON horse_health_metrics (horse_id, recorded_at);
GO
CREATE INDEX idx_inv_tx_zone_supply ON inventory_transactions (zone_id, supply_id, transaction_date);
GO
CREATE INDEX idx_plan_horse ON training_plans (horse_id, status);
GO
CREATE INDEX idx_session_horse_date ON training_sessions (horse_id, session_date);
GO
CREATE INDEX idx_session_date ON training_sessions (session_date);
GO
CREATE INDEX idx_reading_session_time ON sensor_readings (session_id, recorded_at);
GO
CREATE INDEX idx_alert_horse ON performance_alerts (horse_id, created_at);
GO
CREATE INDEX idx_incident_status ON incident_reports (status);
GO
CREATE INDEX idx_diet_horse ON diet_plans (horse_id, status);
GO
CREATE INDEX idx_task_schedule ON care_tasks (scheduled_at, status);
GO
CREATE INDEX idx_medical_horse ON medical_records (horse_id, exam_date);
GO
CREATE INDEX idx_injury_status ON injuries (status);
GO
CREATE INDEX idx_lock_horse_status ON training_locks (horse_id, status);
GO
CREATE INDEX idx_care_due ON preventive_care_schedules (due_date, status);
GO
CREATE INDEX idx_expense_horse_date ON expenses (horse_id, expense_date);
GO
CREATE INDEX idx_report_owner ON owner_reports (owner_id, period_start);
GO


-- ============================================================
-- CHECK constraints converted from MySQL ENUM columns
-- ============================================================
ALTER TABLE users ADD CONSTRAINT CK_users_status CHECK ([status] IN (N'ACTIVE', N'INACTIVE', N'LOCKED'));
GO
ALTER TABLE audit_logs ADD CONSTRAINT CK_audit_logs_action CHECK ([action] IN (N'CREATE', N'UPDATE', N'DELETE', N'LOGIN', N'LOGOUT', N'APPROVE', N'REJECT', N'OTHER'));
GO
ALTER TABLE notifications ADD CONSTRAINT CK_notifications_type CHECK ([type] IN (N'PREVENTIVE_CARE', N'PERFORMANCE_ALERT', N'TRAINING_LOCK', N'INCIDENT', N'SUPPLY_REQUEST', N'RACE', N'REPORT', N'SYSTEM'));
GO
ALTER TABLE stalls ADD CONSTRAINT CK_stalls_stall_type CHECK ([stall_type] IN (N'STANDARD', N'FOALING', N'QUARANTINE', N'RECOVERY'));
GO
ALTER TABLE stalls ADD CONSTRAINT CK_stalls_status CHECK ([status] IN (N'AVAILABLE', N'OCCUPIED', N'MAINTENANCE'));
GO
ALTER TABLE horses ADD CONSTRAINT CK_horses_gender CHECK ([gender] IN (N'STALLION', N'MARE', N'GELDING', N'COLT', N'FILLY'));
GO
ALTER TABLE horses ADD CONSTRAINT CK_horses_health_status CHECK ([health_status] IN (N'ELIGIBLE', N'MONITORING', N'INJURED', N'QUARANTINE'));
GO
ALTER TABLE horses ADD CONSTRAINT CK_horses_readiness_status CHECK ([readiness_status] IN (N'READY', N'NOT_READY', N'RESTING'));
GO
ALTER TABLE horses ADD CONSTRAINT CK_horses_status CHECK ([status] IN (N'ACTIVE', N'RETIRED', N'SOLD', N'DECEASED'));
GO
ALTER TABLE supply_categories ADD CONSTRAINT CK_supply_categories_category_type CHECK ([category_type] IN (N'FEED', N'MEDICINE', N'EQUIPMENT'));
GO
ALTER TABLE supplies ADD CONSTRAINT CK_supplies_status CHECK ([status] IN (N'ACTIVE', N'INACTIVE'));
GO
ALTER TABLE supply_requests ADD CONSTRAINT CK_supply_requests_status CHECK ([status] IN (N'PENDING', N'APPROVED', N'REJECTED', N'FULFILLED'));
GO
ALTER TABLE inventory_transactions ADD CONSTRAINT CK_inventory_transactions_transaction_type CHECK ([transaction_type] IN (N'IMPORT', N'EXPORT', N'ADJUSTMENT', N'CONSUMPTION'));
GO
ALTER TABLE training_plans ADD CONSTRAINT CK_training_plans_status CHECK ([status] IN (N'DRAFT', N'ACTIVE', N'COMPLETED', N'CANCELLED'));
GO
ALTER TABLE training_phases ADD CONSTRAINT CK_training_phases_phase_type CHECK ([phase_type] IN (N'BASE', N'BUILD', N'PEAK', N'TAPER', N'RECOVERY'));
GO
ALTER TABLE training_phases ADD CONSTRAINT CK_training_phases_track_surface CHECK ([track_surface] IN (N'DIRT', N'TURF', N'SYNTHETIC', N'SAND'));
GO
ALTER TABLE training_sessions ADD CONSTRAINT CK_training_sessions_session_type CHECK ([session_type] IN (N'WORKOUT', N'TRIAL_RUN', N'RECOVERY', N'REST'));
GO
ALTER TABLE training_sessions ADD CONSTRAINT CK_training_sessions_intensity CHECK ([intensity] IN (N'LIGHT', N'MODERATE', N'HEAVY'));
GO
ALTER TABLE training_sessions ADD CONSTRAINT CK_training_sessions_track_surface CHECK ([track_surface] IN (N'DIRT', N'TURF', N'SYNTHETIC', N'SAND'));
GO
ALTER TABLE training_sessions ADD CONSTRAINT CK_training_sessions_status CHECK ([status] IN (N'SCHEDULED', N'IN_PROGRESS', N'COMPLETED', N'CANCELLED', N'BLOCKED'));
GO
ALTER TABLE performance_alerts ADD CONSTRAINT CK_performance_alerts_alert_type CHECK ([alert_type] IN (N'HEART_RATE_THRESHOLD', N'SPEED_THRESHOLD', N'FATIGUE', N'INJURY_RISK'));
GO
ALTER TABLE performance_alerts ADD CONSTRAINT CK_performance_alerts_severity CHECK ([severity] IN (N'LOW', N'MEDIUM', N'HIGH', N'CRITICAL'));
GO
ALTER TABLE incident_reports ADD CONSTRAINT CK_incident_reports_incident_type CHECK ([incident_type] IN (N'LOSS_OF_APPETITE', N'COLIC', N'FEVER', N'HOOF_SCRATCH', N'LAMENESS', N'OTHER'));
GO
ALTER TABLE incident_reports ADD CONSTRAINT CK_incident_reports_severity CHECK ([severity] IN (N'LOW', N'MEDIUM', N'HIGH', N'CRITICAL'));
GO
ALTER TABLE incident_reports ADD CONSTRAINT CK_incident_reports_status CHECK ([status] IN (N'OPEN', N'IN_REVIEW', N'RESOLVED'));
GO
ALTER TABLE diet_plans ADD CONSTRAINT CK_diet_plans_status CHECK ([status] IN (N'DRAFT', N'APPROVED', N'INACTIVE'));
GO
ALTER TABLE diet_plan_items ADD CONSTRAINT CK_diet_plan_items_meal_type CHECK ([meal_type] IN (N'BREAKFAST', N'LUNCH', N'DINNER', N'SNACK'));
GO
ALTER TABLE care_tasks ADD CONSTRAINT CK_care_tasks_task_type CHECK ([task_type] IN (N'FEEDING', N'STALL_CLEANING', N'BATHING', N'ICE_BATH', N'GROOMING', N'WALKING', N'OTHER'));
GO
ALTER TABLE care_tasks ADD CONSTRAINT CK_care_tasks_status CHECK ([status] IN (N'PENDING', N'DONE', N'SKIPPED'));
GO
ALTER TABLE medical_records ADD CONSTRAINT CK_medical_records_health_status_after CHECK ([health_status_after] IN (N'ELIGIBLE', N'MONITORING', N'INJURED', N'QUARANTINE'));
GO
ALTER TABLE treatment_plans ADD CONSTRAINT CK_treatment_plans_status CHECK ([status] IN (N'ACTIVE', N'COMPLETED', N'CANCELLED'));
GO
ALTER TABLE prescription_items ADD CONSTRAINT CK_prescription_items_route CHECK ([route] IN (N'ORAL', N'INJECTION', N'TOPICAL', N'IV', N'OTHER'));
GO
ALTER TABLE injuries ADD CONSTRAINT CK_injuries_body_system CHECK ([body_system] IN (N'MUSCLE', N'BONE', N'TENDON', N'LIGAMENT', N'HOOF', N'SKIN', N'OTHER'));
GO
ALTER TABLE injuries ADD CONSTRAINT CK_injuries_severity CHECK ([severity] IN (N'MINOR', N'MODERATE', N'SEVERE'));
GO
ALTER TABLE injuries ADD CONSTRAINT CK_injuries_status CHECK ([status] IN (N'ACTIVE', N'RECOVERING', N'HEALED'));
GO
ALTER TABLE training_locks ADD CONSTRAINT CK_training_locks_lock_level CHECK ([lock_level] IN (N'FULL', N'HEAVY_ONLY'));
GO
ALTER TABLE training_locks ADD CONSTRAINT CK_training_locks_status CHECK ([status] IN (N'ACTIVE', N'RELEASED'));
GO
ALTER TABLE preventive_care_schedules ADD CONSTRAINT CK_preventive_care_schedules_care_type CHECK ([care_type] IN (N'VACCINATION', N'DEWORMING', N'FARRIER', N'DENTAL', N'OTHER'));
GO
ALTER TABLE preventive_care_schedules ADD CONSTRAINT CK_preventive_care_schedules_status CHECK ([status] IN (N'PENDING', N'DONE', N'OVERDUE', N'CANCELLED'));
GO
ALTER TABLE tournaments ADD CONSTRAINT CK_tournaments_status CHECK ([status] IN (N'UPCOMING', N'ONGOING', N'FINISHED', N'CANCELLED'));
GO
ALTER TABLE races ADD CONSTRAINT CK_races_track_surface CHECK ([track_surface] IN (N'DIRT', N'TURF', N'SYNTHETIC', N'SAND'));
GO
ALTER TABLE races ADD CONSTRAINT CK_races_status CHECK ([status] IN (N'OPEN', N'CLOSED', N'FINISHED', N'CANCELLED'));
GO
ALTER TABLE race_registrations ADD CONSTRAINT CK_race_registrations_status CHECK ([status] IN (N'PENDING', N'APPROVED', N'REJECTED', N'WITHDRAWN'));
GO
ALTER TABLE expenses ADD CONSTRAINT CK_expenses_category CHECK ([category] IN (N'FEED', N'MEDICAL', N'TRAINING', N'STABLE', N'RACE_FEE', N'TRANSPORT', N'OTHER'));
GO

ALTER TABLE audit_logs ADD CONSTRAINT CK_audit_logs_old_value_json
CHECK (old_value IS NULL OR ISJSON(old_value) = 1);
GO

ALTER TABLE audit_logs ADD CONSTRAINT CK_audit_logs_new_value_json
CHECK (new_value IS NULL OR ISJSON(new_value) = 1);
GO
