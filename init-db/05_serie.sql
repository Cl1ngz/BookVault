CREATE TABLE serie (
    id_serii SERIAL PRIMARY KEY,
    nazwa VARCHAR(150) NOT NULL,
    liczba_tomow SMALLINT,
    id_autora INT REFERENCES autorzy(id_autora) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_serie_modtime BEFORE UPDATE ON serie 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
