ALTER TABLE account
    ADD COLUMN email_verified boolean NOT NULL DEFAULT true;

ALTER TABLE account
    ALTER COLUMN email_verified SET DEFAULT false;
