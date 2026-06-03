DO
$$
BEGIN
    IF
(
SELECT count(*)
FROM ksiazki) = 0 THEN
SET search_path TO biblioteka, public;

-- A. Natural Authors
INSERT INTO autorzy (imie, nazwisko, narodowosc)
SELECT (ARRAY['Andrzej', 'Olga', 'Jacek', 'Remigiusz', 'Katarzyna', 'Stanisław'])[floor(random() * 6 + 1)],
            (ARRAY['Sapkowski', 'Tokarczuk', 'Dukaj', 'Mróz', 'Bonda', 'Lem'])[floor(random() * 6 + 1)],
            'Polska'
FROM generate_series(1, 10);

-- B. Natural Publishers (without address relations)
INSERT INTO wydawnictwa (nazwa, rok_zalozenia, wlasciciel)
SELECT
    (ARRAY['Znak', 'Wydawnictwo Literackie', 'Rebis', 'Czarna Owca', 'Muza'])[floor(random() * 5 + 1)],
    floor(random() * 40 + 1980),
    (ARRAY['Jan Kowalski', 'Anna Nowak', 'Marek Rebis', 'Helena Owca', 'Piotr Muzowski'])[floor(random() * 5 + 1)]
FROM generate_series(1, 5);

-- C. Natural Book Titles
-- nastroj = mood in StoryGraph style (reading atmosphere):
--   dark, adventurous, mysterious, emotional, funny, tense, hopeful, challenging, reflective, lighthearted
-- NOTE: mood ≠ genre! Fantasy/Sci-Fi are genres, not moods.
INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, rok_wydania, ilosc_stron, nastroj)
SELECT (ARRAY['Cień wiatru', 'Prawiek i inne czasy', 'Cyberiada', 'Lód', 'Chłopcy z Placu Broni', 'Solaris'])[floor(random() * 6 + 1)] || ' - Tom ' || i,
            (SELECT id_autora FROM autorzy ORDER BY random() LIMIT 1),
            (SELECT id_wydawnictwa FROM wydawnictwa ORDER BY random() LIMIT 1),
            floor(random() * 25 + 2000),
            floor(random() * 450 + 150),
            (ARRAY['dark', 'adventurous', 'mysterious', 'emotional', 'funny', 'tense', 'hopeful', 'challenging', 'reflective', 'lighthearted'])[floor(random() * 10 + 1)]
FROM generate_series(1, 20) s(i);

RAISE
NOTICE 'Database seeded with natural data!';
-- SEQUENCE SYNCHRONISATION
PERFORM setval(
    pg_get_serial_sequence('ksiazki', 'id_ksiazki'),
    COALESCE((SELECT MAX(id_ksiazki) FROM ksiazki), 1),
    true
);

PERFORM setval(
    pg_get_serial_sequence('autorzy', 'id_autora'),
    COALESCE((SELECT MAX(id_autora) FROM autorzy), 1),
    true
);

PERFORM setval(
    pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'),
    COALESCE((SELECT MAX(id_wydawnictwa) FROM wydawnictwa), 1),
    true
);

PERFORM setval(
    pg_get_serial_sequence('czytelnicy', 'id_czytelnika'),
    COALESCE((SELECT MAX(id_czytelnika) FROM czytelnicy), 1),
    true
);

PERFORM setval(
    pg_get_serial_sequence('serie', 'id_serii'),
    COALESCE((SELECT MAX(id_serii) FROM serie), 1),
    true
);
END IF;

        -- D. Literary genres (StoryGraph/Goodreads style)
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
NOTICE 'Genres added!';
        PERFORM
setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), MAX(id_gatunku)) FROM gatunki;
END IF;


    -- E. Assign genres to books based on TITLE (not mood!)
    --    Each book gets 1-2 genres matching its actual literary category
    --    Runs when ksiazka_gatunek is empty
    IF
(
SELECT count(*)
FROM ksiazka_gatunek) = 0 AND (SELECT count(*) FROM ksiazki) > 0 THEN

-- Primary genre per title
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

-- Secondary genre per title
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
NOTICE 'Genres assigned to books!';
END IF;
END $$;
