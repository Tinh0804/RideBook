-- Migration: Add scheduled booking support
-- Corresponding Flyway migration: Backend/app/src/main/resources/db/migration/V3__add_scheduled_booking.sql

-- 1. Thêm giá trị QUEUED vào enum bookingstatus nếu chưa có
ALTER TYPE public.bookingstatus ADD VALUE IF NOT EXISTS 'QUEUED';

-- 2. Thêm cột scheduled_at vào bảng booking nếu chưa có
ALTER TABLE public.booking
    ADD COLUMN IF NOT EXISTS scheduled_at timestamp(6) without time zone;

-- 3. Tạo index tối ưu cho việc quét các chuyến hẹn giờ
CREATE INDEX IF NOT EXISTS idx_booking_status_scheduled_at
    ON public.booking (booking_status, scheduled_at);
