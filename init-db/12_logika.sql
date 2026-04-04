-- Funkcja obliczająca średnią ocenę książki
CREATE OR REPLACE FUNCTION pobierz_srednia_ocena(p_id_ksiazki INT) 
RETURNS NUMERIC AS $$
BEGIN
    RETURN (SELECT ROUND(AVG(ocena), 2) FROM recenzje WHERE id_ksiazki = p_id_ksiazki);
END;
$$ LANGUAGE plpgsql;

-- Procedura dodawania czytelnika z od razu przypisanym ulubionym gatunkiem
CREATE OR REPLACE PROCEDURE zarejestruj_czytelnika(
    p_imie VARCHAR, p_nazwisko VARCHAR, p_gatunek_id INT
) AS $$
DECLARE
    v_id_czytelnika INT;
BEGIN
    INSERT INTO czytelnicy (imie, nazwisko) VALUES (p_imie, p_nazwisko) 
    RETURNING id_czytelnika INTO v_id_czytelnika;

    INSERT INTO czytelnik_gatunek (id_czytelnika, id_gatunku) 
    VALUES (v_id_czytelnika, p_gatunek_id);
    
    RAISE NOTICE 'Zarejestrowano czytelnika o ID: %', v_id_czytelnika;
END;
$$ LANGUAGE plpgsql;
