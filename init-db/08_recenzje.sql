CREATE TABLE recenzje (
    id_recenzji SERIAL PRIMARY KEY,
    id_ksiazki INT REFERENCES ksiazki(id_ksiazki) ON DELETE CASCADE,
    id_czytelnika INT REFERENCES czytelnicy(id_czytelnika) ON DELETE CASCADE,
    ocena SMALLINT NOT NULL CHECK (ocena >= 1 AND ocena <= 5),
    tresc TEXT,
    data_dodania TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);