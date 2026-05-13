-- 18_seeder_discworld.sql
-- Seeder z rzeczywistymi danymi książek z cyklu Discworld / Świat Dysku Terry'ego Pratchetta.
-- Założenia:
-- 1. Jeden autor: Terry Pratchett.
-- 2. Jedna seria: Discworld, bez rozbijania na podserie.
-- 3. Jedno wydawnictwo techniczne dla tych rekordów: Corgi Books / Transworld.
-- 4. Liczba stron jest przyjęta według popularnych anglojęzycznych wydań papierowych.
--    W praktyce liczba stron może różnić się między wydaniami UK/US, hardcover/paperback/ebook.

SET search_path TO biblioteka, public;

DO
$$
DECLARE
    v_autor_id INT;
    v_wydawnictwo_id INT;
    v_seria_id INT;
    v_fantasy_id INT;
    v_comic_fantasy_id INT;
    v_satire_id INT;
    v_humor_id INT;
    v_adventure_id INT;
    v_young_adult_id INT;
BEGIN
    -- Autor
    SELECT id_autora
    INTO v_autor_id
    FROM autorzy
    WHERE imie = 'Terry' AND nazwisko = 'Pratchett'
    ORDER BY id_autora
    LIMIT 1;

    IF v_autor_id IS NULL THEN
        INSERT INTO autorzy (imie, nazwisko, data_urodzenia, narodowosc, biografia)
        VALUES (
            'Terry',
            'Pratchett',
            DATE '1948-04-28',
            'Wielka Brytania',
            'Brytyjski pisarz fantasy, autor cyklu Discworld.'
        )
        RETURNING id_autora INTO v_autor_id;
    END IF;

    -- Wydawnictwo. Używamy jednego wspólnego wpisu, żeby nie komplikować modelu wydaniami.
    SELECT id_wydawnictwa
    INTO v_wydawnictwo_id
    FROM wydawnictwa
    WHERE nazwa = 'Corgi Books / Transworld'
    ORDER BY id_wydawnictwa
    LIMIT 1;

    IF v_wydawnictwo_id IS NULL THEN
        INSERT INTO wydawnictwa (nazwa, rok_zalozenia, wlasciciel)
        VALUES ('Corgi Books / Transworld', 1950, 'Penguin Random House')
        RETURNING id_wydawnictwa INTO v_wydawnictwo_id;
    END IF;

    -- Seria główna Discworld
    SELECT id_serii
    INTO v_seria_id
    FROM serie
    WHERE nazwa = 'Discworld' AND id_autora = v_autor_id
    ORDER BY id_serii
    LIMIT 1;

    IF v_seria_id IS NULL THEN
        INSERT INTO serie (nazwa, liczba_tomow, id_autora)
        VALUES ('Discworld', 41, v_autor_id)
        RETURNING id_serii INTO v_seria_id;
    END IF;

    -- Gatunki potrzebne dla książek Discworld
    INSERT INTO gatunki (nazwa)
    VALUES
        ('Fantasy'),
        ('Comic Fantasy'),
        ('Satire'),
        ('Humor'),
        ('Adventure'),
        ('Young Adult')
    ON CONFLICT (nazwa) DO NOTHING;

    SELECT id_gatunku INTO v_fantasy_id FROM gatunki WHERE nazwa = 'Fantasy';
    SELECT id_gatunku INTO v_comic_fantasy_id FROM gatunki WHERE nazwa = 'Comic Fantasy';
    SELECT id_gatunku INTO v_satire_id FROM gatunki WHERE nazwa = 'Satire';
    SELECT id_gatunku INTO v_humor_id FROM gatunki WHERE nazwa = 'Humor';
    SELECT id_gatunku INTO v_adventure_id FROM gatunki WHERE nazwa = 'Adventure';
    SELECT id_gatunku INTO v_young_adult_id FROM gatunki WHERE nazwa = 'Young Adult';

    -- Książki Discworld w kolejności publikacji
    INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, id_serii, rok_wydania, ilosc_stron, nastroj)
    SELECT d.tytul, v_autor_id, v_wydawnictwo_id, v_seria_id, d.rok_wydania, d.ilosc_stron, d.nastroj
    FROM (
        VALUES
            ('The Colour of Magic', 1983, 288, 'funny'),
            ('The Light Fantastic', 1986, 288, 'adventurous'),
            ('Equal Rites', 1987, 288, 'funny'),
            ('Mort', 1987, 320, 'reflective'),
            ('Sourcery', 1988, 288, 'adventurous'),
            ('Wyrd Sisters', 1988, 368, 'funny'),
            ('Pyramids', 1989, 384, 'reflective'),
            ('Guards! Guards!', 1989, 384, 'funny'),
            ('Eric', 1990, 160, 'funny'),
            ('Moving Pictures', 1990, 400, 'funny'),
            ('Reaper Man', 1991, 352, 'reflective'),
            ('Witches Abroad', 1991, 368, 'adventurous'),
            ('Small Gods', 1992, 400, 'reflective'),
            ('Lords and Ladies', 1992, 400, 'mysterious'),
            ('Men at Arms', 1993, 432, 'mysterious'),
            ('Soul Music', 1994, 432, 'funny'),
            ('Interesting Times', 1994, 384, 'adventurous'),
            ('Maskerade', 1995, 384, 'mysterious'),
            ('Feet of Clay', 1996, 416, 'mysterious'),
            ('Hogfather', 1996, 432, 'reflective'),
            ('Jingo', 1997, 432, 'tense'),
            ('The Last Continent', 1998, 416, 'adventurous'),
            ('Carpe Jugulum', 1998, 432, 'dark'),
            ('The Fifth Elephant', 1999, 464, 'mysterious'),
            ('The Truth', 2000, 448, 'reflective'),
            ('Thief of Time', 2001, 432, 'reflective'),
            ('The Last Hero', 2001, 176, 'adventurous'),
            ('The Amazing Maurice and His Educated Rodents', 2001, 304, 'dark'),
            ('Night Watch', 2002, 480, 'dark'),
            ('The Wee Free Men', 2003, 320, 'adventurous'),
            ('Monstrous Regiment', 2003, 496, 'reflective'),
            ('A Hat Full of Sky', 2004, 352, 'adventurous'),
            ('Going Postal', 2004, 496, 'funny'),
            ('Thud!', 2005, 464, 'tense'),
            ('Wintersmith', 2006, 384, 'mysterious'),
            ('Making Money', 2007, 480, 'funny'),
            ('Unseen Academicals', 2009, 544, 'funny'),
            ('I Shall Wear Midnight', 2010, 432, 'dark'),
            ('Snuff', 2011, 480, 'tense'),
            ('Raising Steam', 2013, 480, 'adventurous'),
            ('The Shepherd''s Crown', 2015, 304, 'reflective')
    ) AS d(tytul, rok_wydania, ilosc_stron, nastroj)
    WHERE NOT EXISTS (
        SELECT 1
        FROM ksiazki k
        WHERE k.tytul = d.tytul
          AND k.id_autora = v_autor_id
          AND k.id_serii = v_seria_id
    );

    -- Podstawowe przypisania gatunków dla całego cyklu
    INSERT INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
    SELECT k.id_ksiazki, g.id_gatunku
    FROM ksiazki k
    CROSS JOIN (
        VALUES
            (v_fantasy_id),
            (v_comic_fantasy_id),
            (v_satire_id),
            (v_humor_id),
            (v_adventure_id)
    ) AS g(id_gatunku)
    WHERE k.id_autora = v_autor_id
      AND k.id_serii = v_seria_id
      AND g.id_gatunku IS NOT NULL
    ON CONFLICT DO NOTHING;

    -- Dodatkowy gatunek dla książek młodzieżowych / Tiffany Aching / Maurice
    INSERT INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
    SELECT k.id_ksiazki, v_young_adult_id
    FROM ksiazki k
    WHERE k.id_autora = v_autor_id
      AND k.id_serii = v_seria_id
      AND k.tytul IN (
          'The Amazing Maurice and His Educated Rodents',
          'The Wee Free Men',
          'A Hat Full of Sky',
          'Wintersmith',
          'I Shall Wear Midnight',
          'The Shepherd''s Crown'
      )
      AND v_young_adult_id IS NOT NULL
    ON CONFLICT DO NOTHING;

    -- Synchronizacja sekwencji po ręcznym/warunkowym seedowaniu
    PERFORM setval(pg_get_serial_sequence('autorzy', 'id_autora'), COALESCE((SELECT MAX(id_autora) FROM autorzy), 1), true);
    PERFORM setval(pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'), COALESCE((SELECT MAX(id_wydawnictwa) FROM wydawnictwa), 1), true);
    PERFORM setval(pg_get_serial_sequence('serie', 'id_serii'), COALESCE((SELECT MAX(id_serii) FROM serie), 1), true);
    PERFORM setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), COALESCE((SELECT MAX(id_gatunku) FROM gatunki), 1), true);
    PERFORM setval(pg_get_serial_sequence('ksiazki', 'id_ksiazki'), COALESCE((SELECT MAX(id_ksiazki) FROM ksiazki), 1), true);

    RAISE NOTICE 'Seeder Discworld zakończony. Dodano/uzupełniono książki z cyklu Discworld.';
END
$$;
