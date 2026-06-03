-- 00_init.sql
CREATE SCHEMA IF NOT EXISTS biblioteka;

-- Set default search_path for the 'user' role
ALTER ROLE "user" SET search_path TO biblioteka, public;

-- Set search_path for the current session (initialisation)
SET search_path TO biblioteka, public;
-- Trigger function for the updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';
