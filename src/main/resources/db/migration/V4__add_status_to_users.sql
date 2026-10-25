-- Add status column to users table with default 'ACTIVE'
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';

-- Ensure all existing users have ACTIVE status
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;
