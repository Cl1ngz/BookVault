CREATE TABLE autorzy
(
    id_autora      SERIAL PRIMARY KEY,
    imie           VARCHAR(100) NOT NULL,
    nazwisko       VARCHAR(100) NOT NULL,
    data_urodzenia DATE,
    narodowosc     VARCHAR(50),
    biografia      TEXT,
    email          VARCHAR(255) UNIQUE,
    password_hash  VARCHAR(255),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER update_autorzy_modtime
    BEFORE UPDATE
    ON autorzy
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
