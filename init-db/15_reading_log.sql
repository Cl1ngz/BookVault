-- 15_reading_log.sql - Reading shelf: track status for each book per reader
CREATE TABLE reading_log (
    id_reading_log SERIAL PRIMARY KEY,
    id_czytelnika  INT REFERENCES czytelnicy(id_czytelnika) ON DELETE CASCADE,
    id_ksiazki     INT REFERENCES ksiazki(id_ksiazki) ON DELETE CASCADE,
    status         VARCHAR(20) NOT NULL DEFAULT 'TO_READ'
                       CHECK (status IN ('TO_READ', 'READING', 'FINISHED', 'DNF')),
    strony_przeczytane INT DEFAULT 0,
    data_rozpoczecia   DATE,
    data_zakonczenia   DATE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_czytelnika, id_ksiazki)
);

CREATE TRIGGER update_reading_log_modtime
    BEFORE UPDATE ON reading_log
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

