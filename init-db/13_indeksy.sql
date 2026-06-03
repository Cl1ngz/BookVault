-- B-Tree index on book titles (book search)
CREATE INDEX idx_ksiazki_tytul ON ksiazki USING btree (tytul);

-- Index on author last name (frequently used in filters)
CREATE INDEX idx_autorzy_nazwisko ON autorzy (nazwisko);

-- Index on mood (for filtering by reading atmosphere)
CREATE INDEX idx_ksiazki_nastroj ON ksiazki (nastroj);
