--  Yearly reading goals per reader
CREATE TABLE cele_czytania (
    id_celu        SERIAL PRIMARY KEY,
    id_czytelnika  INT REFERENCES czytelnicy(id_czytelnika) ON DELETE CASCADE,
    rok            INT NOT NULL,
    cel_ksiazek    INT NOT NULL CHECK (cel_ksiazek > 0),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_czytelnika, rok)
);

CREATE TRIGGER update_cele_czytania_modtime
    BEFORE UPDATE ON cele_czytania
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

