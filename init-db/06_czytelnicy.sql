CREATE TABLE czytelnicy
(
    id_czytelnika  SERIAL PRIMARY KEY,
    username       VARCHAR(100) NOT NULL,
    email          VARCHAR(255) UNIQUE,
    password_hash  VARCHAR(255),
    data_urodzenia DATE,
    narodowosc     VARCHAR(50),
    role           VARCHAR(20) NOT NULL DEFAULT 'USER',
    banned_until   DATE NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER update_czytelnicy_modtime
    BEFORE UPDATE
    ON czytelnicy
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
