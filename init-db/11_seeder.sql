-- 11_seeder.sql
DO
$$
BEGIN
    IF
(
SELECT count(*)
FROM ksiazki) = 0 THEN
SET search_path TO biblioteka, public;

-- A. Naturalni Autorzy
INSERT INTO autorzy (imie, nazwisko, narodowosc)
SELECT (ARRAY['Andrzej', 'Olga', 'Jacek', 'Remigiusz', 'Katarzyna', 'Stanisław'])[floor(random() * 6 + 1)],
            (ARRAY['Sapkowski', 'Tokarczuk', 'Dukaj', 'Mróz', 'Bonda', 'Lem'])[floor(random() * 6 + 1)],
            'Polska'
FROM generate_series(1, 10);

-- B. Naturalne Wydawnictwa
INSERT INTO wydawnictwa (nazwa, id_adresu, rok_zalozenia)
SELECT (ARRAY['Znak', 'Wydawnictwo Literackie', 'Rebis', 'Czarna Owca', 'Muza'])[floor(random() * 5 + 1)],
            (SELECT id_adresu FROM adresy ORDER BY random() LIMIT 1),
            floor(random() * 40 + 1980)
FROM generate_series(1, 5);

-- C. Naturalne Tytuły Książek
-- nastroj = mood w stylu StoryGraph (klimat czytania):
--   dark, adventurous, mysterious, emotional, funny, tense, hopeful, challenging, reflective, lighthearted
-- UWAGA: mood ≠ gatunek! Fantasy/Sci-Fi to gatunki, nie moody.
INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, rok_wydania, ilosc_stron, nastroj)
SELECT (ARRAY['Cień wiatru', 'Prawiek i inne czasy', 'Cyberiada', 'Lód', 'Chłopcy z Placu Broni', 'Solaris'])[floor(random() * 6 + 1)] || ' - Tom ' || i,
            (SELECT id_autora FROM autorzy ORDER BY random() LIMIT 1),
            (SELECT id_wydawnictwa FROM wydawnictwa ORDER BY random() LIMIT 1),
            floor(random() * 25 + 2000),
            floor(random() * 450 + 150),
            (ARRAY['dark', 'adventurous', 'mysterious', 'emotional', 'funny', 'tense', 'hopeful', 'challenging', 'reflective', 'lighthearted'])[floor(random() * 10 + 1)]
FROM generate_series(1, 20) s(i);

RAISE
NOTICE 'Baza została nakarmiona naturalnymi danymi!';
        -- SYNCHRONIZACJA SEKWENCJI (Tylko jeśli dane zostały wstawione)
        PERFORM
setval(pg_get_serial_sequence('ksiazki', 'id_ksiazki'), MAX(id_ksiazki)) FROM ksiazki;
        PERFORM
setval(pg_get_serial_sequence('autorzy', 'id_autora'), MAX(id_autora)) FROM autorzy;
        PERFORM
setval(pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'), MAX(id_wydawnictwa)) FROM wydawnictwa;
        PERFORM
setval(pg_get_serial_sequence('czytelnicy', 'id_czytelnika'), MAX(id_czytelnika)) FROM czytelnicy;
        PERFORM
setval(pg_get_serial_sequence('serie', 'id_serii'), MAX(id_serii)) FROM serie;
END IF;

        -- D. Gatunki literackie (StoryGraph/Goodreads style)
    IF
(
SELECT count(*)
FROM gatunki) = 0 THEN
INSERT
INTO gatunki (nazwa)
VALUES
    -- Fiction genres
    ('Fantasy'), ('Science Fiction'), ('Romance'), ('Horror'), ('Thriller'), ('Mystery'), ('Crime'), ('Historical Fiction'), ('Adventure'), ('Literary Fiction'), ('Contemporary Fiction'), ('Magical Realism'), ('Dystopian'), ('Speculative Fiction'), ('Paranormal'), ('Urban Fantasy'), ('Epic Fantasy'), ('Dark Fantasy'), ('Space Opera'), ('Cyberpunk'), ('Steampunk'), ('Alternate History'), ('Satire'), ('Humor'), ('Drama'), ('Coming of Age'), ('Women''s Fiction'), ('Chick Lit'), ('Erotica'), ('Fairy Tale'), ('Mythology'), ('Short Stories'), ('Anthology'),
    -- Non-fiction genres
    ('Biography'), ('Autobiography'), ('Memoir'), ('Self-Help'), ('Psychology'), ('Philosophy'), ('History'), ('Politics'), ('True Crime'), ('Science'), ('Popular Science'), ('Technology'), ('Business'), ('Economics'), ('Travel'), ('Food & Cooking'), ('Art'), ('Music'), ('Sports'), ('Health & Wellness'), ('Parenting'), ('Religion & Spirituality'), ('Essays'),
    -- Young Adult / Children
    ('Young Adult'), ('Middle Grade'), ('Children''s'), ('Picture Book'),
    -- Other
    ('Graphic Novel'), ('Manga'), ('Poetry'), ('Play / Drama')
ON CONFLICT (nazwa) DO NOTHING;

RAISE
NOTICE 'Gatunki zostały dodane!';
        PERFORM
setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), MAX(id_gatunku)) FROM gatunki;
END IF;


    -- E. Przypisanie gatunków do książek na podstawie TYTUŁU (nie moodu!)
    --    Każda książka dostaje 1-2 gatunki odpowiadające rzeczywistej kategorii literackiej
    --    Uruchamia się gdy ksiazka_gatunek jest pusta
    IF
(
SELECT count(*)
FROM ksiazka_gatunek) = 0 AND (SELECT count(*) FROM ksiazki) > 0 THEN

-- Główny gatunek per tytuł
INSERT
INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
SELECT k.id_ksiazki, g.id_gatunku
FROM ksiazki k
         JOIN gatunki g ON g.nazwa = CASE
                                         WHEN k.tytul LIKE 'Cyberiada%' THEN 'Science Fiction'
                                         WHEN k.tytul LIKE 'Solaris%' THEN 'Science Fiction'
                                         WHEN k.tytul LIKE 'Lód%' THEN 'Speculative Fiction'
                                         WHEN k.tytul LIKE 'Prawiek i inne czasy%' THEN 'Speculative Fiction'
                                         WHEN k.tytul LIKE 'Cień wiatru%' THEN 'Romance'
                                         WHEN k.tytul LIKE 'Chłopcy z Placu Broni%' THEN 'Adventure'
    END ON CONFLICT DO NOTHING;

-- Drugi gatunek per tytuł
INSERT INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
SELECT k.id_ksiazki, g.id_gatunku
FROM ksiazki k
         JOIN gatunki g ON g.nazwa = CASE
                                         WHEN k.tytul LIKE 'Cyberiada%' THEN 'Humor'
                                         WHEN k.tytul LIKE 'Solaris%' THEN 'Horror'
                                         WHEN k.tytul LIKE 'Lód%' THEN 'Historical Fiction'
                                         WHEN k.tytul LIKE 'Prawiek i inne czasy%' THEN 'Drama'
                                         WHEN k.tytul LIKE 'Cień wiatru%' THEN 'Crime'
                                         WHEN k.tytul LIKE 'Chłopcy z Placu Broni%' THEN 'Historical Fiction'
    END ON CONFLICT DO NOTHING;

RAISE
NOTICE 'Gatunki zostały przypisane do książek!';
END IF;
END $$;
