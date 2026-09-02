ALTER TYPE public.bookingstatus ADD VALUE IF NOT EXISTS 'QUEUED';

ALTER TABLE public.booking
    ADD COLUMN IF NOT EXISTS scheduled_at timestamp(6) without time zone;

CREATE INDEX IF NOT EXISTS idx_booking_status_scheduled_at
    ON public.booking (booking_status, scheduled_at);
