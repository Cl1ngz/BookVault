CREATE TABLE wydawnictwa (
    id_wydawnictwa SERIAL PRIMARY KEY,
    nazwa VARCHAR(150) NOT NULL,
    rok_zalozenia INT,
    wlasciciel VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_wydawnictwa_modtime BEFORE UPDATE ON wydawnictwa 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
