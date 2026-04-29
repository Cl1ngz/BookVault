CREATE TABLE recenzje (
    id_recenzji SERIAL PRIMARY KEY,
    id_ksiazki INT REFERENCES ksiazki(id_ksiazki) ON DELETE CASCADE,
    id_czytelnika INT REFERENCES czytelnicy(id_czytelnika) ON DELETE CASCADE,
    ocena INTEGER NOT NULL CHECK (ocena >= 1 AND ocena <= 5),
    tresc TEXT,
    data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_recenzje_modtime
    BEFORE UPDATE ON recenzje
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
