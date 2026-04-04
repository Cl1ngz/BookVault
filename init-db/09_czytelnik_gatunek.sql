CREATE TABLE czytelnik_gatunek (
    id_czytelnika INT REFERENCES czytelnicy(id_czytelnika) ON DELETE CASCADE,
    id_gatunku INT REFERENCES gatunki(id_gatunku) ON DELETE CASCADE,
    PRIMARY KEY (id_czytelnika, id_gatunku) --klucz złożony
);