CREATE TABLE adresy (
    id_adresu SERIAL PRIMARY KEY,
    ulica VARCHAR(255),
    numer_domu VARCHAR(10) NOT NULL,
    kod_pocztowy VARCHAR(10) NOT NULL,
    miasto VARCHAR(100) NOT NULL,
    kraj VARCHAR(100) NOT NULL,
    -- MUSISZ DODAĆ TE DWIE LINIE:
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- I TRIGGER (jeśli chcesz automat):
CREATE TRIGGER update_adresy_modtime 
    BEFORE UPDATE ON adresy 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
