CREATE TABLE ksiazka_gatunek (
    id_ksiazki INT REFERENCES ksiazki(id_ksiazki) ON DELETE CASCADE,
    id_gatunku INT REFERENCES gatunki(id_gatunku) ON DELETE CASCADE,
    PRIMARY KEY (id_ksiazki, id_gatunku) --klucz złożony
);