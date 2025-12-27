-- ============================================
-- RESET & CREATE DATABASE SCHEMA
-- Hệ Thống Giám Sát Máy Tính
-- SQL Server
-- ============================================

/* =========================
   DROP TABLES (đúng thứ tự FK)
   ========================= */

IF OBJECT_ID('screen_data', 'U') IS NOT NULL
    DROP TABLE screen_data;

IF OBJECT_ID('notifications', 'U') IS NOT NULL
    DROP TABLE notifications;

IF OBJECT_ID('commands', 'U') IS NOT NULL
    DROP TABLE commands;

IF OBJECT_ID('machines', 'U') IS NOT NULL
    DROP TABLE machines;




/* =========================
   CREATE TABLE: MACHINES
   ========================= */
CREATE TABLE machines (
    machine_id NVARCHAR(100) PRIMARY KEY,
    name NVARCHAR(255),
    ip_address NVARCHAR(50),
    os_name NVARCHAR(100),
    os_version NVARCHAR(100),
    is_online BIT DEFAULT 0,
    last_response_time DATETIME,
    registered_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_machines_online ON machines(is_online);
CREATE INDEX idx_machines_ip ON machines(ip_address);




/* =========================
   CREATE TABLE: COMMANDS
   ========================= */
CREATE TABLE commands (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    machine_id NVARCHAR(100) NOT NULL,
    command_type NVARCHAR(50) NOT NULL,
    command_data NVARCHAR(MAX),
    status NVARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT GETDATE(),
    executed_at DATETIME,
    response_data NVARCHAR(MAX),

    CONSTRAINT FK_commands_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(machine_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_commands_machine ON commands(machine_id);
CREATE INDEX idx_commands_status ON commands(status);
CREATE INDEX idx_commands_type ON commands(command_type);
CREATE INDEX idx_commands_created ON commands(created_at);




/* =========================
   CREATE TABLE: NOTIFICATIONS
   ========================= */
CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    machine_id NVARCHAR(100) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    title NVARCHAR(255),
    notification_type NVARCHAR(50) DEFAULT 'INFO',
    sent_at DATETIME DEFAULT GETDATE(),
    displayed_at DATETIME,

    CONSTRAINT FK_notifications_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(machine_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_notifications_machine ON notifications(machine_id);
CREATE INDEX idx_notifications_sent ON notifications(sent_at);




/* =========================
   CREATE TABLE: SCREEN_DATA
   (FIX multiple cascade paths)
   ========================= */
CREATE TABLE screen_data (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    machine_id NVARCHAR(100) NOT NULL,
    image_data VARBINARY(MAX) NOT NULL,
    image_format NVARCHAR(20) DEFAULT 'PNG',
    captured_at DATETIME DEFAULT GETDATE(),
    command_id BIGINT NULL,

    CONSTRAINT FK_screen_data_machine
        FOREIGN KEY (machine_id)
        REFERENCES machines(machine_id)
        ON DELETE NO ACTION,

    CONSTRAINT FK_screen_data_command
        FOREIGN KEY (command_id)
        REFERENCES commands(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_screen_data_machine ON screen_data(machine_id);
CREATE INDEX idx_screen_data_captured ON screen_data(captured_at);
CREATE INDEX idx_screen_data_command ON screen_data(command_id);
