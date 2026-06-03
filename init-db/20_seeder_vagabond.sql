SET search_path TO biblioteka, public;
DO
$$
DECLARE
    v_autor_id INT;
    v_wydawnictwo_id INT;
    v_seria_id INT;
    v_manga_id INT;
    v_historical_fiction_id INT;
    v_martial_arts_id INT;
    v_seinen_id INT;
    v_drama_id INT;
    v_adventure_id INT;
    v_action_id INT;
    v_psychological_id INT;
    v_philosophical_id INT;
    v_biographical_id INT;
BEGIN
    -- Author
    SELECT id_autora
    INTO v_autor_id
    FROM autorzy
    WHERE imie = 'Takehiko' AND nazwisko = 'Inoue'
    ORDER BY id_autora
    LIMIT 1;
    IF v_autor_id IS NULL THEN
        INSERT INTO autorzy (imie, nazwisko, data_urodzenia, narodowosc, biografia)
        VALUES (
            'Takehiko',
            'Inoue',
            DATE '1967-01-12',
            'Japan',
            'Japanese manga artist, author of Vagabond, Slam Dunk and Real. Vagabond is his interpretation of the story of Miyamoto Musashi.'
        )
        RETURNING id_autora INTO v_autor_id;
    END IF;
    -- Original Japanese publisher
    SELECT id_wydawnictwa
    INTO v_wydawnictwo_id
    FROM wydawnictwa
    WHERE nazwa = 'Kodansha'
    ORDER BY id_wydawnictwa
    LIMIT 1;
    IF v_wydawnictwo_id IS NULL THEN
        INSERT INTO wydawnictwa (nazwa, rok_zalozenia, wlasciciel)
        VALUES ('Kodansha', 1909, 'Kodansha Ltd.')
        RETURNING id_wydawnictwa INTO v_wydawnictwo_id;
    END IF;
    -- Main Vagabond series
    SELECT id_serii
    INTO v_seria_id
    FROM serie
    WHERE nazwa = 'Vagabond' AND id_autora = v_autor_id
    ORDER BY id_serii
    LIMIT 1;
    IF v_seria_id IS NULL THEN
        INSERT INTO serie (nazwa, liczba_tomow, id_autora)
        VALUES ('Vagabond', 37, v_autor_id)
        RETURNING id_serii INTO v_seria_id;
    ELSE
        UPDATE serie
        SET liczba_tomow = 37
        WHERE id_serii = v_seria_id
          AND (liczba_tomow IS NULL OR liczba_tomow < 37);
    END IF;
    -- Genres needed for Vagabond
    INSERT INTO gatunki (nazwa)
    VALUES
        ('Manga'),
        ('Historical Fiction'),
        ('Martial Arts'),
        ('Seinen'),
        ('Drama'),
        ('Adventure'),
        ('Action'),
        ('Psychological'),
        ('Philosophical'),
        ('Biographical Fiction')
    ON CONFLICT (nazwa) DO NOTHING;
    SELECT id_gatunku INTO v_manga_id FROM gatunki WHERE nazwa = 'Manga';
    SELECT id_gatunku INTO v_historical_fiction_id FROM gatunki WHERE nazwa = 'Historical Fiction';
    SELECT id_gatunku INTO v_martial_arts_id FROM gatunki WHERE nazwa = 'Martial Arts';
    SELECT id_gatunku INTO v_seinen_id FROM gatunki WHERE nazwa = 'Seinen';
    SELECT id_gatunku INTO v_drama_id FROM gatunki WHERE nazwa = 'Drama';
    SELECT id_gatunku INTO v_adventure_id FROM gatunki WHERE nazwa = 'Adventure';
    SELECT id_gatunku INTO v_action_id FROM gatunki WHERE nazwa = 'Action';
    SELECT id_gatunku INTO v_psychological_id FROM gatunki WHERE nazwa = 'Psychological';
    SELECT id_gatunku INTO v_philosophical_id FROM gatunki WHERE nazwa = 'Philosophical';
    SELECT id_gatunku INTO v_biographical_id FROM gatunki WHERE nazwa = 'Biographical Fiction';
    -- Vagabond volumes in original Japanese publication order
    INSERT INTO ksiazki (tytul, id_autora, id_wydawnictwa, id_serii, rok_wydania, ilosc_stron, nastroj)
    SELECT d.tytul, v_autor_id, v_wydawnictwo_id, v_seria_id, d.rok_wydania, d.ilosc_stron, d.nastroj
    FROM (
        VALUES
            ('Vagabond Vol. 1', 1999, 246, 'intense'),
            ('Vagabond Vol. 2', 1999, 238, 'intense'),
            ('Vagabond Vol. 3', 1999, 232, 'adventurous'),
            ('Vagabond Vol. 4', 1999, 216, 'tense'),
            ('Vagabond Vol. 5', 2000, 216, 'tense'),
            ('Vagabond Vol. 6', 2000, 216, 'reflective'),
            ('Vagabond Vol. 7', 2000, 216, 'philosophical'),
            ('Vagabond Vol. 8', 2000, 216, 'reflective'),
            ('Vagabond Vol. 9', 2001, 196, 'philosophical'),
            ('Vagabond Vol. 10', 2001, 216, 'tense'),
            ('Vagabond Vol. 11', 2001, 216, 'reflective'),
            ('Vagabond Vol. 12', 2001, 216, 'mysterious'),
            ('Vagabond Vol. 13', 2002, 216, 'tense'),
            ('Vagabond Vol. 14', 2002, 216, 'reflective'),
            ('Vagabond Vol. 15', 2002, 216, 'dramatic'),
            ('Vagabond Vol. 16', 2003, 216, 'violent'),
            ('Vagabond Vol. 17', 2003, 208, 'dramatic'),
            ('Vagabond Vol. 18', 2003, 208, 'intense'),
            ('Vagabond Vol. 19', 2004, 208, 'melancholic'),
            ('Vagabond Vol. 20', 2004, 208, 'reflective'),
            ('Vagabond Vol. 21', 2005, 216, 'tense'),
            ('Vagabond Vol. 22', 2006, 216, 'tragic'),
            ('Vagabond Vol. 23', 2006, 216, 'tense'),
            ('Vagabond Vol. 24', 2006, 216, 'anticipatory'),
            ('Vagabond Vol. 25', 2007, 216, 'dramatic'),
            ('Vagabond Vol. 26', 2007, 216, 'violent'),
            ('Vagabond Vol. 27', 2007, 216, 'exhausted'),
            ('Vagabond Vol. 28', 2008, 216, 'reflective'),
            ('Vagabond Vol. 29', 2008, 216, 'philosophical'),
            ('Vagabond Vol. 30', 2009, 216, 'melancholic'),
            ('Vagabond Vol. 31', 2009, 216, 'reflective'),
            ('Vagabond Vol. 32', 2010, 216, 'philosophical'),
            ('Vagabond Vol. 33', 2010, 216, 'quiet'),
            ('Vagabond Vol. 34', 2012, 208, 'reflective'),
            ('Vagabond Vol. 35', 2013, 208, 'contemplative'),
            ('Vagabond Vol. 36', 2013, 208, 'contemplative'),
            ('Vagabond Vol. 37', 2014, 208, 'reflective')
    ) AS d(tytul, rok_wydania, ilosc_stron, nastroj)
    WHERE NOT EXISTS (
        SELECT 1
        FROM ksiazki k
        WHERE k.tytul = d.tytul
          AND k.id_autora = v_autor_id
          AND k.id_serii = v_seria_id
    );
    -- Base genre assignments for the entire series
    INSERT INTO ksiazka_gatunek (id_ksiazki, id_gatunku)
    SELECT k.id_ksiazki, g.id_gatunku
    FROM ksiazki k
    CROSS JOIN (
        VALUES
            (v_manga_id),
            (v_historical_fiction_id),
            (v_martial_arts_id),
            (v_seinen_id),
            (v_drama_id),
            (v_adventure_id),
            (v_action_id),
            (v_psychological_id),
            (v_philosophical_id),
            (v_biographical_id)
    ) AS g(id_gatunku)
    WHERE k.id_autora = v_autor_id
      AND k.id_serii = v_seria_id
      AND g.id_gatunku IS NOT NULL
    ON CONFLICT DO NOTHING;
    -- Synchronise sequences after conditional/manual seeding
    PERFORM setval(pg_get_serial_sequence('autorzy', 'id_autora'), COALESCE((SELECT MAX(id_autora) FROM autorzy), 1), true);
    PERFORM setval(pg_get_serial_sequence('wydawnictwa', 'id_wydawnictwa'), COALESCE((SELECT MAX(id_wydawnictwa) FROM wydawnictwa), 1), true);
    PERFORM setval(pg_get_serial_sequence('serie', 'id_serii'), COALESCE((SELECT MAX(id_serii) FROM serie), 1), true);
    PERFORM setval(pg_get_serial_sequence('gatunki', 'id_gatunku'), COALESCE((SELECT MAX(id_gatunku) FROM gatunki), 1), true);
    PERFORM setval(pg_get_serial_sequence('ksiazki', 'id_ksiazki'), COALESCE((SELECT MAX(id_ksiazki) FROM ksiazki), 1), true);
    RAISE NOTICE 'Vagabond seeder completed. Volumes of the Vagabond series added/updated.';
END
$$;
