CREATE TABLE ksiazki (
    id_ksiazki SERIAL PRIMARY KEY,
    tytul VARCHAR(255) NOT NULL,
    id_autora INT REFERENCES autorzy(id_autora) ON DELETE CASCADE, -- Cascade delete
    id_wydawnictwa INT REFERENCES wydawnictwa(id_wydawnictwa) ON DELETE SET NULL, -- Safe detach
    id_serii INT REFERENCES serie(id_serii) ON DELETE SET NULL,
    rok_wydania INT,
    ilosc_stron INT,
    nastroj VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_ksiazki_modtime BEFORE UPDATE ON ksiazki 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
