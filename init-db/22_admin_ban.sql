-- Add banned_until column to czytelnicy for ban functionality
ALTER TABLE biblioteka.czytelnicy
    ADD COLUMN IF NOT EXISTS banned_until DATE NULL;

