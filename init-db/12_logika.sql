-- Function to calculate the average rating of a book
DROP FUNCTION IF EXISTS biblioteka.pobierz_srednia_ocena(integer);

CREATE FUNCTION biblioteka.pobierz_srednia_ocena(p_id_ksiazki INT)
RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN (SELECT ROUND(AVG(ocena)::numeric, 2) FROM recenzje WHERE id_ksiazki = p_id_ksiazki);
END;
$$ LANGUAGE plpgsql;

COMMIT;
CREATE OR REPLACE PROCEDURE zarejestruj_czytelnika(
    p_username VARCHAR,
    p_email VARCHAR,
    p_password_hash VARCHAR
) AS $$
BEGIN
    INSERT INTO czytelnicy (username, email, password_hash, role)
    VALUES (p_username, p_email, p_password_hash, 'USER');
END;
$$ LANGUAGE plpgsql;