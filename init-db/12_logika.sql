-- Function to calculate the average rating of a book
DROP FUNCTION IF EXISTS biblioteka.pobierz_srednia_ocena(integer);

CREATE FUNCTION biblioteka.pobierz_srednia_ocena(p_id_ksiazki INT)
RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN (SELECT ROUND(AVG(ocena)::numeric, 2) FROM recenzje WHERE id_ksiazki = p_id_ksiazki);
END;
$$ LANGUAGE plpgsql;

COMMIT;
-- Procedure to register a reader with an assigned favourite genre
CREATE OR REPLACE PROCEDURE zarejestruj_czytelnika(
    p_username VARCHAR, p_gatunek_id INT
) AS $$
DECLARE
v_id_czytelnika INT;
BEGIN
INSERT INTO czytelnicy (username) VALUES (p_username)
    RETURNING id_czytelnika INTO v_id_czytelnika;

INSERT INTO czytelnik_gatunek (id_czytelnika, id_gatunku)
VALUES (v_id_czytelnika, p_gatunek_id);

RAISE NOTICE 'Reader registered with ID: %', v_id_czytelnika;
END;
$$ LANGUAGE plpgsql;
