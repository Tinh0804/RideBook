
CREATE DATABASE "RideBookDB";
\c "RideBookDB";


CREATE TYPE BookingStatus AS ENUM ('ACCEPTED', 'ARRIVED', 'CANCELLED', 'COMPLETED', 'IN_PROGRESS', 'PENDING');
CREATE CAST (VARCHAR AS BookingStatus) WITH INOUT AS IMPLICIT;
CREATE CAST (BookingStatus AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE RejectionType AS ENUM ('IGNORED', 'REJECTED');
CREATE CAST (VARCHAR AS RejectionType) WITH INOUT AS IMPLICIT;
CREATE CAST (RejectionType AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE CustomerPromotionStatus AS ENUM ('EXPIRED', 'SAVED', 'USED');
CREATE CAST (VARCHAR AS CustomerPromotionStatus) WITH INOUT AS IMPLICIT;
CREATE CAST (CustomerPromotionStatus AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE DiscountType AS ENUM ('FIXED_AMOUNT', 'PERCENTAGE');
CREATE CAST (VARCHAR AS DiscountType) WITH INOUT AS IMPLICIT;
CREATE CAST (DiscountType AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE TransactionStatus AS ENUM ('CANCELLED', 'COMPLETED', 'FAILED', 'PENDING');
CREATE CAST (VARCHAR AS TransactionStatus) WITH INOUT AS IMPLICIT;
CREATE CAST (TransactionStatus AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE TransactionType AS ENUM ('DEPOSIT', 'PAYMENT', 'TRIP_FEE', 'TRIP_INCOME', 'WITHDRAWAL');
CREATE CAST (VARCHAR AS TransactionType) WITH INOUT AS IMPLICIT;
CREATE CAST (TransactionType AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE Provider AS ENUM ('FACEBOOK', 'GOOGLE', 'LOCAL');
CREATE CAST (VARCHAR AS Provider) WITH INOUT AS IMPLICIT;
CREATE CAST (Provider AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE PaymentMethod AS ENUM ('CASH', 'MOMO', 'ONLINE', 'VNPAY');
CREATE CAST (VARCHAR AS PaymentMethod) WITH INOUT AS IMPLICIT;
CREATE CAST (PaymentMethod AS VARCHAR) WITH INOUT AS IMPLICIT;

CREATE TYPE WalletStatus AS ENUM ('ACTIVE', 'BLOCKED', 'INACTIVE');
CREATE CAST (VARCHAR AS WalletStatus) WITH INOUT AS IMPLICIT;
CREATE CAST (WalletStatus AS VARCHAR) WITH INOUT AS IMPLICIT;

create type PredefinedRole as ENUM('ADMIN','CUSTOMER','DRIVER');
CREATE CAST (VARCHAR AS PredefinedRole) WITH INOUT AS IMPLICIT;
CREATE CAST (PredefinedRole AS VARCHAR) WITH INOUT AS IMPLICIT


CREATE TABLE role (
    role_id VARCHAR(36) PRIMARY KEY,
    description VARCHAR(255),
    role_name VARCHAR(255)
);

CREATE TABLE time (
    time_id VARCHAR(36) PRIMARY KEY,
    start_time TIME(6),
    end_time TIME(6),
    slot_name VARCHAR(255)
);

CREATE TABLE vehicle_type (
    vehicle_type_id VARCHAR(36) PRIMARY KEY,
    max_passengers INTEGER,
    price_per_km FLOAT(53),
    icon VARCHAR(255),
    vehicle_type_name VARCHAR(255)
);

CREATE TABLE payment (
    payment_id VARCHAR(36) PRIMARY KEY,
    amount FLOAT(53),
    payment_status boolean,
    payment_type PaymentMethod
);

CREATE TABLE promotion (
    promotion_id VARCHAR(36) PRIMARY KEY,
    discount_limit FLOAT(53),
    discount_value FLOAT(53),
    is_active BOOLEAN,
    min_trip_value FLOAT(53),
    quantity INTEGER,
    usage_limit_per_user INTEGER,
    start_time TIMESTAMP(6),
    end_time TIMESTAMP(6),
    application_condition VARCHAR(255),
    promotion_code VARCHAR(255),
    promotion_image VARCHAR(255),
    promotion_name VARCHAR(255),
    discount_type DiscountType
);

CREATE TABLE invalid_token (
    id VARCHAR(255) PRIMARY KEY,
    expiry_time TIMESTAMP(6) NOT NULL,
    reason VARCHAR(255) NOT NULL
);

CREATE TABLE account (
    account_id VARCHAR(36) PRIMARY KEY,
    role_id VARCHAR(36) NOT NULL REFERENCES role(role_id),
    user_name VARCHAR(100) NOT NULL UNIQUE,
    pass_word VARCHAR(100) NOT NULL,
    provider Provider,
    provider_id VARCHAR(255),
    account_status WalletStatus,
    created_at TIMESTAMP(6)
);

CREATE TABLE customer (
    customer_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) UNIQUE REFERENCES account(account_id),
    phone VARCHAR(15) UNIQUE,
    customer_name VARCHAR(255),
    birth_date DATE,
    address VARCHAR(255),
    avatar VARCHAR(255),
    email VARCHAR(255),
    gender VARCHAR(255)
);

CREATE TABLE driver (
    driver_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) UNIQUE REFERENCES account(account_id),
    vehicle_type_id VARCHAR(36) REFERENCES vehicle_type(vehicle_type_id),
    phone VARCHAR(15) UNIQUE,
    email VARCHAR(100) UNIQUE,
    citizen_id VARCHAR(200) UNIQUE,
    license_plate VARCHAR(20) UNIQUE,
    driver_name VARCHAR(255),
    birth_date DATE,
    driving_license VARCHAR(200),
    vehicle_name VARCHAR(255),
    activity_status boolean,
    current_lat FLOAT(53),
    current_lng FLOAT(53),
    score FLOAT(53),
    last_trip_time TIMESTAMP(6),
    address VARCHAR(255),
    area VARCHAR(255),
    avatar VARCHAR(255),
    criminal_record VARCHAR(255),
    gender VARCHAR(255)
);

CREATE TABLE vehicle_type_time (
    time_id VARCHAR(255) NOT NULL REFERENCES time(time_id),
    vehicle_type_id VARCHAR(255) NOT NULL REFERENCES vehicle_type(vehicle_type_id),
    surcharge FLOAT(53),
    PRIMARY KEY (time_id, vehicle_type_id)
);

CREATE TABLE booking (
    booking_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    driver_id VARCHAR(36) REFERENCES driver(driver_id),
    payment_id VARCHAR(36) REFERENCES payment(payment_id),
    promotion_id VARCHAR(36) REFERENCES promotion(promotion_id),
    vehicle_type_id VARCHAR(36) REFERENCES vehicle_type(vehicle_type_id),
    booking_status BookingStatus,
    distance FLOAT(53),
    original_price FLOAT(53),
    total_price FLOAT(53),
    pickup_lat FLOAT(53),
    pickup_lng FLOAT(53),
    dropoff_lat FLOAT(53),
    dropoff_lng FLOAT(53),
    pickup_location VARCHAR(255),
    dropoff_location VARCHAR(255),
    booking_time TIMESTAMP(6),
    pickup_time TIMESTAMP(6),
    arrival_time TIMESTAMP(6)
);

CREATE TABLE booking_rejection (
    rejection_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL REFERENCES booking(booking_id),
    driver_id VARCHAR(36) NOT NULL REFERENCES driver(driver_id),
    rejection_type RejectionType NOT NULL,
    rejected_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE customer_promotion (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) REFERENCES customer(customer_id),
    promotion_id VARCHAR(36) REFERENCES promotion(promotion_id),
    status CustomerPromotionStatus,
    saved_at TIMESTAMP(6),
    used_at TIMESTAMP(6)
);

CREATE TABLE notification (
    notification_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) REFERENCES account(account_id),
    booking_id VARCHAR(36) REFERENCES booking(booking_id),
    title VARCHAR(255),
    message VARCHAR(255),
    is_read BOOLEAN,
    sent_at TIMESTAMP(6)
);

CREATE TABLE rating (
    rating_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) REFERENCES booking(booking_id),
    score FLOAT(53),
    rating_type VARCHAR(255),
    review VARCHAR(255),
    created_at TIMESTAMP(6)
);

CREATE TABLE wallet (
    wallet_id VARCHAR(36) PRIMARY KEY,
    driver_id VARCHAR(36) UNIQUE REFERENCES driver(driver_id),
    balance FLOAT(53),
    status WalletStatus
);

CREATE TABLE wallet_transaction (
    transaction_id VARCHAR(255) PRIMARY KEY,
    wallet_id VARCHAR(36) REFERENCES wallet(wallet_id),
    reference_id VARCHAR(255),
    amount FLOAT(53),
    status TransactionStatus,
    type TransactionType,
    created_at TIMESTAMP(6)
);

CREATE TABLE chat_message (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36),
    sender_id VARCHAR(36),
    receiver_id VARCHAR(36),
    content TEXT,
    timestamp TIMESTAMP(6)
);



-- ============================================================
-- Chèn dữ liệu mẫu cho RideBookDB - Sử dụng UUID chuẩn
-- ============================================================

-- 1. Bảng role
INSERT INTO role (role_id, description, role_name) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Quản trị viên hệ thống', 'ADMIN'),
('550e8400-e29b-41d4-a716-446655440001', 'Khách hàng sử dụng dịch vụ', 'CUSTOMER'),
('550e8400-e29b-41d4-a716-446655440002', 'Tài xế lái xe', 'DRIVER');

-- 2. Bảng time (khung giờ)
INSERT INTO time (time_id, start_time, end_time, slot_name) VALUES
('6d3f8c2a-8b1d-4b7e-9a5f-4b3a8f2d1e01', '06:00:00', '09:00:00', 'Sáng sớm'),
('7a4c9d3b-2e5f-4c8d-9b1a-2c3d4e5f6a02', '09:00:00', '12:00:00', 'Buổi sáng'),
('8b5a0e4c-3f6a-5d9e-0c2b-3d4e5f6a7b03', '12:00:00', '14:00:00', 'Buổi trưa'),
('9c6b1f5d-4a7b-6e0f-1d3c-4e5f6a7b8c04', '14:00:00', '17:00:00', 'Buổi chiều'),
('0d7c2a6e-5b8c-7f1a-2e4d-5f6a7b8c9d05', '17:00:00', '20:00:00', 'Buổi tối'),
('1e8d3b7f-6c9d-8a2b-3f5e-6a7b8c9d0e06', '20:00:00', '23:59:59', 'Đêm khuya');

-- 3. Bảng vehicle_type
INSERT INTO vehicle_type (vehicle_type_id, max_passengers, price_per_km, icon, vehicle_type_name) VALUES
('a1b2c3d4-e5f6-7890-1234-567890abcde1', 4, 12500, 'https://cdn-icons-png.flaticon.com/128/1048/1048315.png', 'Xe ô tô 4 chỗ'),
('b2c3d4e5-f6a7-8901-2345-67890abcdef2', 7, 18.0, 'https://cdn-icons-png.flaticon.com/128/1048/1048313.png', 'Xe SUV 7 chỗ'),
('c3d4e5f6-a7b8-9012-3456-7890abcdef03', 12, 22.0, 'https://cdn-icons-png.flaticon.com/128/1048/1048333.png', 'Xe Van 12 chỗ'),
('d4e5f6a7-b8c9-0123-4567-890abcdef014', 1, 8.0, 'https://cdn-icons-png.flaticon.com/128/1023/1023346.png', 'Xe máy');

-- 4. Bảng payment
INSERT INTO payment (payment_id, amount, payment_status, payment_type) VALUES
('f0e1d2c3-b4a5-6978-9a0b-1c2d3e4f5678', 150000, true, 'CASH'),
('a1b2c3d4-e5f6-7890-1234-567890abcdef', 210000, true, 'MOMO'),
('b2c3d4e5-f6a7-8901-2345-67890abcdef1', 95000, true, 'ONLINE'),
('c3d4e5f6-a7b8-9012-3456-7890abcdef12', 0, false, 'VNPAY');

-- 5. Bảng promotion
INSERT INTO promotion (
    promotion_id, discount_limit, discount_value, is_active, min_trip_value,
    quantity, usage_limit_per_user, start_time, end_time,
    application_condition, promotion_code, promotion_image, promotion_name, discount_type
) VALUES
('p1a2b3c4-d5e6-7890-1234-567890abcd01', 50000, 20, true, 100000, 100, 3,
 '2025-01-01 00:00:00', '2025-12-31 23:59:59',
 'Áp dụng cho tất cả', 'SAVE20', 'promo_save20.png', 'Giảm 20% tối đa 50k', 'PERCENTAGE'),
('p2b3c4d5-e6f7-8901-2345-67890abcd012', 30000, 30000, true, 80000, 50, 2,
 '2025-02-01 00:00:00', '2025-03-01 23:59:59',
 'Chỉ áp dụng buổi sáng', 'FIX30K', 'promo_fix30k.png', 'Giảm 30k cố định', 'FIXED_AMOUNT');

-- 6. Bảng account
INSERT INTO account (account_id, role_id, user_name, pass_word, provider, provider_id, account_status, created_at) VALUES
('ac1a2b3c-d4e5-6789-0123-4567890abcde', '550e8400-e29b-41d4-a716-446655440000', 'admin', 'admin123', 'LOCAL', NULL, 'ACTIVE', NOW()),
('ac2b3c4d-e5f6-7890-1234-567890abcdef', '550e8400-e29b-41d4-a716-446655440001', 'customer1', 'pass123', 'LOCAL', NULL, 'ACTIVE', NOW()),
('ac3c4d5e-f6a7-8901-2345-67890abcdef1', '550e8400-e29b-41d4-a716-446655440001', 'customer2', 'pass456', 'GOOGLE', 'google_12345', 'ACTIVE', NOW()),
('ac4d5e6f-a7b8-9012-3456-7890abcdef12', '550e8400-e29b-41d4-a716-446655440002', 'driver1', 'driver123', 'LOCAL', NULL, 'ACTIVE', NOW()),
('ac5e6f7a-b8c9-0123-4567-890abcdef123', '550e8400-e29b-41d4-a716-446655440002', 'driver2', 'driver456', 'FACEBOOK', 'fb_67890', 'BLOCKED', NOW());

-- 7. Bảng customer
INSERT INTO customer (customer_id, account_id, phone, customer_name, birth_date, address, avatar, email, gender) VALUES
('cu1a2b3c-d4e5-6789-0123-4567890abcde', 'ac2b3c4d-e5f6-7890-1234-567890abcdef', '0901234567', 'Nguyễn Văn A', '1990-05-15', '123 Đường Lê Lợi, Quận 1, TP.HCM', 'avatar_a.png', 'vana@example.com', 'Nam'),
('cu2b3c4d-e5f6-7890-1234-567890abcdef', 'ac3c4d5e-f6a7-8901-2345-67890abcdef1', '0909876543', 'Trần Thị B', '1985-08-22', '456 Đường Nguyễn Huệ, Quận 3, TP.HCM', 'avatar_b.png', 'thib@example.com', 'Nữ');

-- 8. Bảng driver
INSERT INTO driver (
    driver_id, account_id, vehicle_type_id, phone, email, citizen_id, license_plate,
    driver_name, birth_date, driving_license, vehicle_name, activity_status,
    current_lat, current_lng, score, last_trip_time, address, area, avatar, criminal_record, gender
) VALUES
('dr1a2b3c-d4e5-6789-0123-4567890abcde', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12', 'a1b2c3d4-e5f6-7890-1234-567890abcde1',
 '0912345678', 'driver1@example.com', '123456789', '51A-12345',
 'Lê Văn C', '1988-03-10', 'DL123456', 'Toyota Vios', 'ACTIVE',
 10.8231, 106.6297, 4.8, '2025-03-10 08:30:00', '789 Đường CMT8, Quận 10', 'Quận 10', 'driver1.png', 'Không', 'Nam'),
('dr2b3c4d-e5f6-7890-1234-567890abcdef', 'ac5e6f7a-b8c9-0123-4567-890abcdef123', 'd4e5f6a7-b8c9-0123-4567-890abcdef014',
 '0923456789', 'driver2@example.com', '987654321', '59B-67890',
 'Phạm Thị D', '1992-07-20', 'DL654321', 'Honda Wave', 'BLOCKED',
 10.7626, 106.6825, 4.2, '2025-03-09 17:15:00', '321 Đường Võ Văn Tần, Quận 3', 'Quận 3', 'driver2.png', 'Có', 'Nữ');

-- 9. Bảng vehicle_type_time
INSERT INTO vehicle_type_time (time_id, vehicle_type_id, surcharge) VALUES
('6d3f8c2a-8b1d-4b7e-9a5f-4b3a8f2d1e01', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.2),
('7a4c9d3b-2e5f-4c8d-9b1a-2c3d4e5f6a02', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.0),
('8b5a0e4c-3f6a-5d9e-0c2b-3d4e5f6a7b03', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.1),
('9c6b1f5d-4a7b-6e0f-1d3c-4e5f6a7b8c04', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.0),
('0d7c2a6e-5b8c-7f1a-2e4d-5f6a7b8c9d05', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.3),
('1e8d3b7f-6c9d-8a2b-3f5e-6a7b8c9d0e06', 'a1b2c3d4-e5f6-7890-1234-567890abcde1', 1.5),
('6d3f8c2a-8b1d-4b7e-9a5f-4b3a8f2d1e01', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.2),
('7a4c9d3b-2e5f-4c8d-9b1a-2c3d4e5f6a02', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.0),
('8b5a0e4c-3f6a-5d9e-0c2b-3d4e5f6a7b03', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.1),
('9c6b1f5d-4a7b-6e0f-1d3c-4e5f6a7b8c04', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.0),
('0d7c2a6e-5b8c-7f1a-2e4d-5f6a7b8c9d05', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.3),
('1e8d3b7f-6c9d-8a2b-3f5e-6a7b8c9d0e06', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2', 1.5),
('6d3f8c2a-8b1d-4b7e-9a5f-4b3a8f2d1e01', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.2),
('7a4c9d3b-2e5f-4c8d-9b1a-2c3d4e5f6a02', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.0),
('8b5a0e4c-3f6a-5d9e-0c2b-3d4e5f6a7b03', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.1),
('9c6b1f5d-4a7b-6e0f-1d3c-4e5f6a7b8c04', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.0),
('0d7c2a6e-5b8c-7f1a-2e4d-5f6a7b8c9d05', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.3),
('1e8d3b7f-6c9d-8a2b-3f5e-6a7b8c9d0e06', 'c3d4e5f6-a7b8-9012-3456-7890abcdef03', 1.5),
('6d3f8c2a-8b1d-4b7e-9a5f-4b3a8f2d1e01', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.1),
('7a4c9d3b-2e5f-4c8d-9b1a-2c3d4e5f6a02', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.0),
('8b5a0e4c-3f6a-5d9e-0c2b-3d4e5f6a7b03', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.0),
('9c6b1f5d-4a7b-6e0f-1d3c-4e5f6a7b8c04', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.0),
('0d7c2a6e-5b8c-7f1a-2e4d-5f6a7b8c9d05', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.2),
('1e8d3b7f-6c9d-8a2b-3f5e-6a7b8c9d0e06', 'd4e5f6a7-b8c9-0123-4567-890abcdef014', 1.3);

-- 10. Bảng booking
INSERT INTO booking (
    booking_id, customer_id, driver_id, payment_id, promotion_id, vehicle_type_id,
    booking_status, distance, original_price, total_price,
    pickup_lat, pickup_lng, dropoff_lat, dropoff_lng,
    pickup_location, dropoff_location, booking_time, pickup_time, arrival_time
) VALUES
('bk1a2b3c-d4e5-6789-0123-4567890abcde', 'cu1a2b3c-d4e5-6789-0123-4567890abcde', 'dr1a2b3c-d4e5-6789-0123-4567890abcde',
 'f0e1d2c3-b4a5-6978-9a0b-1c2d3e4f5678', 'p1a2b3c4-d5e6-7890-1234-567890abcd01', 'a1b2c3d4-e5f6-7890-1234-567890abcde1',
 'COMPLETED', 8.5, 106250, 85000,
 10.8231, 106.6297, 10.7626, 106.6825,
 '123 Lê Lợi, Q1', '321 Võ Văn Tần, Q3', '2025-03-10 08:00:00', '2025-03-10 08:15:00', '2025-03-10 08:10:00'),
('bk2b3c4d-e5f6-7890-1234-567890abcdef', 'cu2b3c4d-e5f6-7890-1234-567890abcdef', 'dr1a2b3c-d4e5-6789-0123-4567890abcde',
 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'p2b3c4d5-e6f7-8901-2345-67890abcd012', 'b2c3d4e5-f6a7-8901-2345-67890abcdef2',
 'IN_PROGRESS', 5.2, 93600, 63600,
 10.7626, 106.6825, 10.8231, 106.6297,
 '321 Võ Văn Tần, Q3', '123 Lê Lợi, Q1', '2025-03-11 14:30:00', '2025-03-11 14:45:00', '2025-03-11 14:40:00'),
('bk3c4d5e-f6a7-8901-2345-67890abcdef1', 'cu1a2b3c-d4e5-6789-0123-4567890abcde', NULL,
 'b2c3d4e5-f6a7-8901-2345-67890abcdef1', NULL, 'd4e5f6a7-b8c9-0123-4567-890abcdef014',
 'PENDING', 3.0, 24000, 24000,
 10.8000, 106.6500, 10.7800, 106.6700,
 '456 Nguyễn Huệ, Q3', '789 CMT8, Q10', '2025-03-12 09:00:00', NULL, NULL),
('bk4d5e6f-a7b8-9012-3456-7890abcdef12', 'cu2b3c4d-e5f6-7890-1234-567890abcdef', 'dr2b3c4d-e5f6-7890-1234-567890abcdef',
 'c3d4e5f6-a7b8-9012-3456-7890abcdef12', NULL, 'a1b2c3d4-e5f6-7890-1234-567890abcde1',
 'CANCELLED', 12.0, 150000, 150000,
 10.8200, 106.6300, 10.7600, 106.6800,
 'Địa điểm A', 'Địa điểm B', '2025-03-12 07:30:00', NULL, NULL);

-- 11. Bảng booking_rejection
INSERT INTO booking_rejection (rejection_id, booking_id, driver_id, rejection_type, rejected_at) VALUES
('rj1a2b3c-d4e5-6789-0123-4567890abcde', 'bk4d5e6f-a7b8-9012-3456-7890abcdef12', 'dr2b3c4d-e5f6-7890-1234-567890abcdef', 'REJECTED', '2025-03-12 07:35:00'),
('rj2b3c4d-e5f6-7890-1234-567890abcdef', 'bk3c4d5e-f6a7-8901-2345-67890abcdef1', 'dr1a2b3c-d4e5-6789-0123-4567890abcde', 'IGNORED', '2025-03-12 09:05:00');

-- 12. Bảng customer_promotion
INSERT INTO customer_promotion (id, customer_id, promotion_id, status, saved_at, used_at) VALUES
('cp1a2b3c-d4e5-6789-0123-4567890abcde', 'cu1a2b3c-d4e5-6789-0123-4567890abcde', 'p1a2b3c4-d5e6-7890-1234-567890abcd01', 'USED', '2025-03-10 07:50:00', '2025-03-10 08:00:00'),
('cp2b3c4d-e5f6-7890-1234-567890abcdef', 'cu2b3c4d-e5f6-7890-1234-567890abcdef', 'p2b3c4d5-e6f7-8901-2345-67890abcd012', 'SAVED', '2025-03-11 10:00:00', NULL),
('cp3c4d5e-f6a7-8901-2345-67890abcdef1', 'cu1a2b3c-d4e5-6789-0123-4567890abcde', 'p2b3c4d5-e6f7-8901-2345-67890abcd012', 'EXPIRED', '2025-02-01 08:00:00', NULL);

-- 13. Bảng notification
INSERT INTO notification (notification_id, account_id, booking_id, title, message, is_read, sent_at) VALUES
('nt1a2b3c-d4e5-6789-0123-4567890abcde', 'ac2b3c4d-e5f6-7890-1234-567890abcdef', 'bk1a2b3c-d4e5-6789-0123-4567890abcde',
 'Chuyến đi hoàn tất', 'Chuyến đi của bạn đã hoàn thành, cảm ơn bạn đã sử dụng dịch vụ.', true, '2025-03-10 08:40:00'),
('nt2b3c4d-e5f6-7890-1234-567890abcdef', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12', 'bk1a2b3c-d4e5-6789-0123-4567890abcde',
 'Đánh giá mới', 'Khách hàng đã đánh giá bạn 5 sao.', false, '2025-03-10 09:00:00'),
('nt3c4d5e-f6a7-8901-2345-67890abcdef1', 'ac3c4d5e-f6a7-8901-2345-67890abcdef1', 'bk2b3c4d-e5f6-7890-1234-567890abcdef',
 'Tài xế đang đến', 'Tài xế Lê Văn C đang trên đường tới đón bạn.', false, '2025-03-11 14:35:00');

-- 14. Bảng rating
INSERT INTO rating (rating_id, booking_id, score, rating_type, review, created_at) VALUES
('rt1a2b3c-d4e5-6789-0123-4567890abcde', 'bk1a2b3c-d4e5-6789-0123-4567890abcde', 5.0, 'DRIVER', 'Tài xế thân thiện, xe sạch sẽ.', '2025-03-10 08:50:00'),
('rt2b3c4d-e5f6-7890-1234-567890abcdef', 'bk1a2b3c-d4e5-6789-0123-4567890abcde', 4.5, 'CUSTOMER', 'Khách hàng lịch sự, trả tiền đúng hẹn.', '2025-03-10 08:55:00');

-- 15. Bảng wallet
INSERT INTO wallet (wallet_id, driver_id, balance, status) VALUES
('wl1a2b3c-d4e5-6789-0123-4567890abcde', 'dr1a2b3c-d4e5-6789-0123-4567890abcde', 350000, 'ACTIVE'),
('wl2b3c4d-e5f6-7890-1234-567890abcdef', 'dr2b3c4d-e5f6-7890-1234-567890abcdef', 120000, 'BLOCKED');

-- 16. Bảng wallet_transaction
INSERT INTO wallet_transaction (transaction_id, wallet_id, reference_id, amount, status, type, created_at) VALUES
('wt1a2b3c-d4e5-6789-0123-4567890abcde', 'wl1a2b3c-d4e5-6789-0123-4567890abcde', 'bk1a2b3c-d4e5-6789-0123-4567890abcde', 85000, 'COMPLETED', 'TRIP_INCOME', '2025-03-10 08:45:00'),
('wt2b3c4d-e5f6-7890-1234-567890abcdef', 'wl1a2b3c-d4e5-6789-0123-4567890abcde', 'bk2b3c4d-e5f6-7890-1234-567890abcdef', 63600, 'PENDING', 'TRIP_INCOME', '2025-03-11 14:50:00'),
('wt3c4d5e-f6a7-8901-2345-67890abcdef1', 'wl2b3c4d-e5f6-7890-1234-567890abcdef', 'bk4d5e6f-a7b8-9012-3456-7890abcdef12', 150000, 'FAILED', 'TRIP_INCOME', '2025-03-12 07:40:00'),
('wt4d5e6f-a7b8-9012-3456-7890abcdef12', 'wl1a2b3c-d4e5-6789-0123-4567890abcde', 'dep_001', 200000, 'COMPLETED', 'DEPOSIT', '2025-03-09 10:00:00');

-- 17. Bảng chat_message
INSERT INTO chat_message (id, booking_id, sender_id, receiver_id, content, timestamp) VALUES
('cm1a2b3c-d4e5-6789-0123-4567890abcde', 'bk1a2b3c-d4e5-6789-0123-4567890abcde', 'ac2b3c4d-e5f6-7890-1234-567890abcdef', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12',
 'Xin chào, tôi đang ở trước cửa nhà.', '2025-03-10 08:10:00'),
('cm2b3c4d-e5f6-7890-1234-567890abcdef', 'bk1a2b3c-d4e5-6789-0123-4567890abcde', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12', 'ac2b3c4d-e5f6-7890-1234-567890abcdef',
 'Vâng tôi đã tới, mời bạn lên xe.', '2025-03-10 08:12:00'),
('cm3c4d5e-f6a7-8901-2345-67890abcdef1', 'bk2b3c4d-e5f6-7890-1234-567890abcdef', 'ac3c4d5e-f6a7-8901-2345-67890abcdef1', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12',
 'Bạn đến đúng giờ nhé.', '2025-03-11 14:30:00'),
('cm4d5e6f-a7b8-9012-3456-7890abcdef12', 'bk2b3c4d-e5f6-7890-1234-567890abcdef', 'ac4d5e6f-a7b8-9012-3456-7890abcdef12', 'ac3c4d5e-f6a7-8901-2345-67890abcdef1',
 'Tôi sẽ tới trong 5 phút nữa.', '2025-03-11 14:32:00');


create or replace function Pr_FindAvailableDriversCloserCustomer (
	lat double precision,
	lng double precision,
	radius double precision,
	p_vehicle_type_id varchar(36) 
)
returns table (
	driver_id VARCHAR(36),
    account_id VARCHAR(36) ,
    vehicle_type_id VARCHAR(36),
    phone VARCHAR(15),
    email VARCHAR(100),
    citizen_id VARCHAR(200),
    license_plate VARCHAR(20),
    driver_name VARCHAR(255),
    birth_date DATE,
    driving_license VARCHAR(200),
    vehicle_name VARCHAR(255),
    current_lat FLOAT(53),
    current_lng FLOAT(53),
    score FLOAT(53),
    last_trip_time TIMESTAMP(6),
    address VARCHAR(255),
    area VARCHAR(255),
    avatar VARCHAR(255),
    criminal_record VARCHAR(255),
    gender VARCHAR(255),
    activity_status BOOLEAN,
    Distance double precision
)
language plpgsql
as $$
begin
	return query
	select d.driver_id, d.account_id, d.vehicle_type_id, d.phone, d.email, d.citizen_id, d.license_plate, d.driver_name, d.birth_date, d.driving_license, d.vehicle_name, d.current_lat, d.current_lng, d.score, d.last_trip_time, d.address, d.area, d.avatar, d.criminal_record, d.gender, d.activity_status, (6371 * acos(
		            cos(radians(lat)) * cos(radians(d.current_lat)) 
		            * cos(radians(d.current_lng) - radians(lng)) 
		            + sin(radians(lat)) * sin(radians(d.current_lat))
		        )) AS Distance
	from driver d
	join vehicle_type vt on vt.vehicle_type_id = d.vehicle_type_id
	where d.activity_status = true
		and vt.vehicle_type_id = p_vehicle_type_id
		and not exists (
			select 1 
			from booking b
			where b.driver_id = d.driver_id and b.booking_status in ('ACCEPTED','ARRIVED','IN_PROGRESS')
		)
		and (6371 * acos(
            cos(radians(lat)) * cos(radians(d.current_lat)) 
            * cos(radians(d.current_lng) - radians(lng)) 
            + sin(radians(lat)) * sin(radians(d.current_lat))
        )) <= radius
	order by Distance asc;
end;
$$;


\df Pr_FindAvailableDriversCloserCustomer

