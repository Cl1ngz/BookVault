SET search_path TO biblioteka, public;

DO
$$
DECLARE
    v_autor_id INT;
    v_wydawnictwo_id INT;
    v_seria_id INT;
    v_manga_id INT;
    v_fantasy_id INT;
    v_dark_fantasy_id INT;
    v_epic_fantasy_id INT;
    v_sword_and_sorcery_id INT;
    v_horror_id INT;
    v_adventure_id INT;
    v_action_id INT;
    v_seinen_id INT;
    v_psychological_id INT;
    v_supernatural_id INT;
    v_tragedy_id INT;

    v_ksiazka_id INT;

    cur_ksiazki_berserk CURSOR FOR
        SELECT id_ksiazki
        FROM ksiazki
        WHERE id_autora = v_autor_id
          AND id_serii = v_seria_id
        ORDER BY id_ksiazki;
BEGIN
    -- Autor
    SELECT id_autora
    INTO v_autor_id
    FROM autorzy
    WHERE imie = 'Kentaro' AND nazwisko = 'Miura'
    ORDER BY id_autora
    LIMIT 1;

    IF v_autor_id IS NULL THEN
        INSERT INTO autorzy (imie, nazwisko, data_urodzenia, narodowosc, biografia)
        VALUES (
            'Kentaro',
            'Miura',
            DATE '1966-07-11',
            'Japonia',
            'Japoński mangaka, twórca mangi Berserk. Po jego śmierci seria jest kontynuowana przez Studio Gaga pod nadzorem Koujiego Moriego.'
        )
        RETURNING id_autora INTO v_autor_id;
    END IF;

    -- Oryginalne japońskie wydawnictwo
    SELECT id_wydawnictwa
    INTO v_wydawnictwo_id
    FROM wydawnictwa
    WHERE nazwa = 'Hakusensha'
    ORDER BY id_wydawnictwa
    LIMIT 1;

    IF v_wydawnictwo_id IS NULL THEN
        INSERT INTO wydawnictwa (nazwa, rok_zalozenia, wlasciciel)
        VALUES ('Hakusensha', 1973, 'Hakusensha, Inc.')
        RETURNING id_wydawnictwa INTO v_wydawnictwo_id;
    END IF;

    -- Seria główna Berserk
    SELECT id_serii
    INTO v_seria_id
    FROM serie
    WHERE nazwa = 'Berserk' AND id_autora = v_autor_id
    ORDER BY id_serii
    LIMIT 1;

    IF v_seria_id IS NULL THEN
        INSERT INTO serie (nazwa, liczba_tomow, id_autora)
        VALUES ('Berserk', 43, v_autor_id)
        RETURNING id_serii INTO v_seria_id;
    ELSE
        UPDATE serie
        SET liczba_tomow = 43
        WHERE id_serii = v_seria_id
          AND (liczba_tomow IS NULL OR liczba_tomow < 43);
    END IF;

    -- Gatunki potrzebne dla Berserka
    INSERT INTO gatunki (nazwa)
    VALUES
        ('Manga'),
        ('Fantasy'),
        ('Dark Fantasy'),
        ('Epic Fantasy'),
        ('Sword and Sorcery'),
        ('Horror'),
        ('Adventure'),
        ('Action'),
        ('Seinen'),
        ('Psychological'),
        ('Supernatural'),
        ('Tragedy')
    ON CONFLICT (nazwa) DO NOTHING;

    SELECT id_gatunku INTO v_manga_id FROM gatunki WHERE nazwa = 'Manga';
    SELECT id_gatunku INTO v_fantasy_id FROM gatunki WHERE nazwa = 'Fantasy';
    SELECT id_gatunku INTO v_dark_fantasy_id FROM gatunki WHERE nazwa = 'Dark Fantasy';
    SELECT id_gatunku INTO v_epic_fantasy_id FROM gatunki WHERE nazwa = 'Epic Fantasy';
    SELECT id_gatunku INTO v_sword_and_sorcery_id FROM gatunki WHERE nazwa = 'Sword and Sorcery';
    SELECT id_gatunku INTO v_horror_id FROM gatunki WHERE nazwa = 'Horror';
    SELECT id_gatunku INTO v_adventure_id FROM gatunki WHERE nazwa = 'Adventure';
    SELECT id_gatunku INTO v_action_id FROM gatunki WHERE nazwa = 'Action';
    SELECT id_gatunku INTO v_seinen_id FROM gatunki WHERE nazwa = 'Seinen';
    SELECT id_gatunku INTO v_psychological_id FROM gatunki WHERE nazwa = 'Psychological';
    SELECT id_gatunku INTO v_supernatural_id FROM gatunki WHERE nazwa = 'Supernatural';
    SELECT id_gatunku INTO v_tragedy_id FROM gatunki WHERE nazwa = 'Tragedy';

    -- Tomy Berserk w kolejności oryginalnego japońskiego wydania
    INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, id_serii, rok_wydania, ilosc_stron, nastroj)
    SELECT d.tytul, v_autor_id, v_wydawnictwo_id, v_seria_id, d.rok_wydania, d.ilosc_stron, d.nastroj
    FROM (
        VALUES
            ('Berserk Vol. 1', 1990, 240, 'dark'),
            ('Berserk Vol. 2', 1991, 240, 'dark'),
            ('Berserk Vol. 3', 1991, 240, 'dark'),
            ('Berserk Vol. 4', 1992, 240, 'tragic'),
            ('Berserk Vol. 5', 1993, 240, 'tragic'),
            ('Berserk Vol. 6', 1993, 240, 'tragic'),
            ('Berserk Vol. 7', 1994, 240, 'tragic'),
            ('Berserk Vol. 8', 1994, 240, 'tragic'),
            ('Berserk Vol. 9', 1995, 240, 'tragic'),
            ('Berserk Vol. 10', 1995, 240, 'tragic'),
            ('Berserk Vol. 11', 1996, 240, 'tragic'),
            ('Berserk Vol. 12', 1996, 240, 'tragic'),
            ('Berserk Vol. 13', 1997, 240, 'horrific'),
            ('Berserk Vol. 14', 1997, 240, 'dark'),
            ('Berserk Vol. 15', 1998, 240, 'dark'),
            ('Berserk Vol. 16', 1998, 240, 'dark'),
            ('Berserk Vol. 17', 1999, 240, 'dark'),
            ('Berserk Vol. 18', 1999, 240, 'dark'),
            ('Berserk Vol. 19', 2000, 240, 'horrific'),
            ('Berserk Vol. 20', 2000, 240, 'horrific'),
            ('Berserk Vol. 21', 2001, 240, 'dark'),
            ('Berserk Vol. 22', 2001, 240, 'dark'),
            ('Berserk Vol. 23', 2002, 240, 'dark'),
            ('Berserk Vol. 24', 2002, 240, 'adventurous'),
            ('Berserk Vol. 25', 2003, 240, 'adventurous'),
            ('Berserk Vol. 26', 2003, 240, 'horrific'),
            ('Berserk Vol. 27', 2004, 240, 'dark'),
            ('Berserk Vol. 28', 2005, 232, 'adventurous'),
            ('Berserk Vol. 29', 2005, 240, 'tense'),
            ('Berserk Vol. 30', 2006, 240, 'tense'),
            ('Berserk Vol. 31', 2006, 240, 'tense'),
            ('Berserk Vol. 32', 2007, 240, 'tense'),
            ('Berserk Vol. 33', 2008, 240, 'tense'),
            ('Berserk Vol. 34', 2009, 232, 'cataclysmic'),
            ('Berserk Vol. 35', 2010, 216, 'adventurous'),
            ('Berserk Vol. 36', 2011, 208, 'adventurous'),
            ('Berserk Vol. 37', 2013, 224, 'reflective'),
            ('Berserk Vol. 38', 2016, 200, 'mysterious'),
            ('Berserk Vol. 39', 2017, 192, 'mysterious'),
            ('Berserk Vol. 40', 2018, 176, 'reflective'),
            ('Berserk Vol. 41', 2021, 176, 'melancholic'),
            ('Berserk Vol. 42', 2023, 192, 'dark'),
            ('Berserk Vol. 43', 2025, 192, 'dark')
    ) AS d(tytul, rok_wydania, ilosc_stron, nastroj)
    WHERE NOT EXISTS (
        SELECT 1
        FROM ksiazki k
        WHERE k.tytul = d.tytul
          AND k.id_autora = v_autor_id
          AND k.id_serii = v_seria_id
    );

      -- Podstawowe przypisania gatunków dla całej serii z użyciem kursora
    OPEN cur_ksiazki_berserk;

    LOOP
        FETCH cur_ksiazki_berserk INTO v_ksiazka_id;

        EXIT WHEN NOT FOUND;

        INSERT INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
        SELECT v_ksiazka_id, g.id_gatunku
        FROM (
            VALUES
                (v_manga_id),
                (v_fantasy_id),
                (v_dark_fantasy_id),
                (v_epic_fantasy_id),
                (v_sword_and_sorcery_id),
                (v_horror_id),
                (v_adventure_id),
                (v_action_id),
                (v_seinen_id),
                (v_psychological_id),
                (v_supernatural_id),
                (v_tragedy_id)
        ) AS g(id_gatunku)
        WHERE g.id_gatunku IS NOT NULL
        ON CONFLICT DO NOTHING;
    END LOOP;

    CLOSE cur_ksiazki_berserk;

    -- Synchronizacja sekwencji po ręcznym/warunkowym seedowaniu
    PERFORM setval(pg_get_serial_sequence('autorzy', 'id_autora'), COALESCE((SELECT MAX(id_autora) FROM autorzy), 1), true);
    PERFORM setval(pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'), COALESCE((SELECT MAX(id_wydawnictwa) FROM wydawnictwa), 1), true);
    PERFORM setval(pg_get_serial_sequence('serie', 'id_serii'), COALESCE((SELECT MAX(id_serii) FROM serie), 1), true);
    PERFORM setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), COALESCE((SELECT MAX(id_gatunku) FROM gatunki), 1), true);
    PERFORM setval(pg_get_serial_sequence('ksiazki', 'id_ksiazki'), COALESCE((SELECT MAX(id_ksiazki) FROM ksiazki), 1), true);

    RAISE NOTICE 'Seeder Berserk zakończony. Dodano/uzupełniono tomy serii Berserk.';
END
$$;
