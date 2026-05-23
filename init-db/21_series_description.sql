-- Add description column to serie table
ALTER TABLE biblioteka.serie ADD COLUMN IF NOT EXISTS opis TEXT;

