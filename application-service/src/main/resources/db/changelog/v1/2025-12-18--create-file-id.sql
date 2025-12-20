ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS file_id uuid;