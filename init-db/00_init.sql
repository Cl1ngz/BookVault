-- 00_init.sql
CREATE SCHEMA IF NOT EXISTS biblioteka;

-- Ustawienie, aby użytkownik 'user' zawsze domyślnie korzystał z tego schematu
ALTER ROLE "user" SET search_path TO biblioteka, public;

-- Ustawienie dla obecnej sesji (inicjalizacji)
SET search_path TO biblioteka, public;
-- Funkcja wyzwalacza dla kolumny updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';
