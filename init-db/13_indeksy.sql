-- Indeks B-Tree na tytuły (szukanie książek)
CREATE INDEX idx_ksiazki_tytul ON ksiazki USING btree (tytul);

-- Indeks na nazwisko autora (często używane przy filtrach)
CREATE INDEX idx_autorzy_nazwisko ON autorzy (nazwisko);

-- Indeks GIN na nastroje (jeśli będziesz chciał szukać po tagach)
CREATE INDEX idx_ksiazki_nastroj ON ksiazki (nastroj);
