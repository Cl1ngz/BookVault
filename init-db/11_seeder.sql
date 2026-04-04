-- 11_seeder.sql
DO $$ 
BEGIN
    IF (SELECT count(*) FROM ksiazki) = 0 THEN
        SET search_path TO biblioteka, public;

        -- A. Naturalni Autorzy
        INSERT INTO autorzy (imie, nazwisko, narodowosc)
        SELECT 
            (ARRAY['Andrzej', 'Olga', 'Jacek', 'Remigiusz', 'Katarzyna', 'Stanisław'])[floor(random() * 6 + 1)],
            (ARRAY['Sapkowski', 'Tokarczuk', 'Dukaj', 'Mróz', 'Bonda', 'Lem'])[floor(random() * 6 + 1)],
            'Polska'
        FROM generate_series(1, 10);

        -- B. Naturalne Wydawnictwa
        INSERT INTO wydawnictwa (nazwa, id_adresu, rok_zalozenia)
        SELECT 
            (ARRAY['Znak', 'Wydawnictwo Literackie', 'Rebis', 'Czarna Owca', 'Muza'])[floor(random() * 5 + 1)],
            (SELECT id_adresu FROM adresy ORDER BY random() LIMIT 1),
            floor(random() * 40 + 1980)
        FROM generate_series(1, 5);

        -- C. Naturalne Tytuły Książek
        INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, rok_wydania, ilosc_stron, nastroj)
        SELECT 
            (ARRAY['Cień wiatru', 'Prawiek i inne czasy', 'Cyberiada', 'Lód', 'Chłopcy z Placu Broni', 'Solaris'])[floor(random() * 6 + 1)] || ' - Tom ' || i,
            (SELECT id_autora FROM autorzy ORDER BY random() LIMIT 1),
            (SELECT id_wydawnictwa FROM wydawnictwa ORDER BY random() LIMIT 1),
            floor(random() * 25 + 2000),
            floor(random() * 450 + 150),
            (ARRAY['romance', 'fantasy', 'dark grimdark', 'sci-fi', 'humor', 'speculative'])[floor(random() * 6 + 1)]
        FROM generate_series(1, 20) s(i);

        RAISE NOTICE 'Baza została nakarmiona naturalnymi danymi!';
	-- SYNCHRONIZACJA SEKWENCJI (Tylko jeśli dane zostały wstawione)
        PERFORM setval(pg_get_serial_sequence('ksiazki', 'id_ksiazki'), MAX(id_ksiazki)) FROM ksiazki;
        PERFORM setval(pg_get_serial_sequence('autorzy', 'id_autora'), MAX(id_autora)) FROM autorzy;
        PERFORM setval(pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'), MAX(id_wydawnictwa)) FROM wydawnictwa;
        PERFORM setval(pg_get_serial_sequence('czytelnicy', 'id_czytelnika'), MAX(id_czytelnika)) FROM czytelnicy;
        PERFORM setval(pg_get_serial_sequence('serie', 'id_serii'), MAX(id_serii)) FROM serie;
        PERFORM setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), MAX(id_gatunku)) FROM gatunki;
    END IF;
END $$;
