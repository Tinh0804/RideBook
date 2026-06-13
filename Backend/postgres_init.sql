
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
    payment_status WalletStatus,
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
    activity_status WalletStatus,
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
