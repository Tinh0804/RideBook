-- Thêm cột used_coins và tier_discount vào Booking
ALTER TABLE booking ADD COLUMN IF NOT EXISTS used_coins INTEGER DEFAULT 0;
ALTER TABLE booking ADD COLUMN IF NOT EXISTS tier_discount DOUBLE PRECISION DEFAULT 0;

-- Tạo Enum cho MembershipTier
DO $$ BEGIN
    CREATE TYPE membershiptier AS ENUM ('BRONZE', 'SILVER', 'GOLD', 'DIAMOND');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Tạo Enum cho PointTransactionType
DO $$ BEGIN
    CREATE TYPE pointtransactiontype AS ENUM ('EARNED', 'REDEEMED', 'EXPIRED', 'BONUS');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Bảng LoyaltyAccount (tích điểm cho account_id, dùng cho cả Customer và Driver)
CREATE TABLE IF NOT EXISTS loyalty_account (
    loyalty_account_id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL UNIQUE,
    current_points INTEGER DEFAULT 0,
    lifetime_points INTEGER DEFAULT 0,
    tier membershiptier DEFAULT 'BRONZE',
    total_spent DOUBLE PRECISION DEFAULT 0,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng PointTransaction (Lịch sử giao dịch điểm)
CREATE TABLE IF NOT EXISTS point_transaction (
    point_transaction_id VARCHAR(36) PRIMARY KEY,
    loyalty_account_id VARCHAR(36) NOT NULL REFERENCES loyalty_account(loyalty_account_id),
    amount INTEGER NOT NULL,
    type pointtransactiontype NOT NULL,
    description VARCHAR(255),
    reference_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng FavoritePlace (Địa điểm yêu thích)
CREATE TABLE IF NOT EXISTS favorite_place (
    favorite_place_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    label VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    icon VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
